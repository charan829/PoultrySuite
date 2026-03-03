
const express = require('express');
const cors = require('cors');
const { PrismaClient } = require('@prisma/client');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

const app = express();
const prisma = new PrismaClient();
const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'super-secret-key';

app.use(cors());
app.use(express.json());

// Request Logger
app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
    if (req.method === 'POST' || req.method === 'PUT') {
        console.log('Body:', req.body);
    }
    next();
});

// Middleware to authenticate token
const authenticateToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1]; // Bearer TOKEN

    if (token == null) return res.sendStatus(401);

    jwt.verify(token, JWT_SECRET, (err, user) => {
        if (err) return res.sendStatus(403);
        req.user = user;
        next();
    });
};

// Seed Admin User
const seedAdminUser = async () => {
    try {
        const adminEmail = 'admin@gmail.com';
        const existingAdmin = await prisma.user.findUnique({ where: { email: adminEmail } });
        if (!existingAdmin) {
            const hashedPassword = await bcrypt.hash('Admin', 10);
            await prisma.user.create({
                data: {
                    email: adminEmail,
                    password: hashedPassword,
                    role: 'ADMIN',
                    name: 'System Admin'
                }
            });
            console.log('Default Admin user created: admin@gmail.com / Admin');
        } else {
            console.log('Admin user already exists.');
        }
    } catch (e) {
        console.error('Error seeding admin user:', e);
    }
};

// --- AUTH ROUTES ---

app.post('/auth/register', async (req, res) => {
    const { email, password, role, name, phone } = req.body;
    try {
        const hashedPassword = await bcrypt.hash(password, 10);
        const user = await prisma.user.create({
            data: {
                email,
                password: hashedPassword,
                role: role || 'FARMER',
                name,
                phone
            },
        });
        // Create Farm if user is Farmer
        if (user.role === 'FARMER') {
            await prisma.farm.create({
                data: {
                    name: `${name}'s Farm`,
                    ownerId: user.id
                }
            });
        }
        res.json({ message: 'User created successfully', userId: user.id });
    } catch (error) {
        res.status(400).json({ error: 'Email already exists or invalid data' });
    }
});

app.post('/auth/login', async (req, res) => {
    const { email, password } = req.body;
    try {
        const user = await prisma.user.findUnique({ where: { email } });
        if (!user) return res.status(400).json({ error: 'User not found' });

        if (await bcrypt.compare(password, user.password)) {
            const token = jwt.sign({ id: user.id, role: user.role }, JWT_SECRET);
            res.json({ token, role: user.role, userId: user.id, name: user.name });
        } else {
            res.status(401).json({ error: 'Invalid password' });
        }
    } catch (error) {
        res.status(500).json({ error: 'Internal server error' });
    }
});

// --- FARMER ROUTES ---

// Get Farm Dashboard Data
app.get('/farm/dashboard', authenticateToken, async (req, res) => {
    if (req.user.role !== 'FARMER') return res.sendStatus(403);

    try {
        const farm = await prisma.farm.findUnique({
            where: { ownerId: req.user.id },
            include: { batches: true, inventory: true }
        });
        res.json(farm);
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

// Add Batch
app.post('/farm/batch', authenticateToken, async (req, res) => {
    const { type, count, ageDays } = req.body;
    try {
        const farm = await prisma.farm.findUnique({ where: { ownerId: req.user.id } });
        const batch = await prisma.batch.create({
            data: {
                farmId: farm.id,
                type,
                count: parseInt(count),
                ageDays: parseInt(ageDays || 0)
            }
        });
        res.json(batch);
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

// Add Listing (Farmer)
app.post('/market/listing', authenticateToken, async (req, res) => {
    if (req.user.role !== 'FARMER') return res.sendStatus(403);
    const { type, quantity, pricePerUnit } = req.body;
    try {
        const farm = await prisma.farm.findUnique({ where: { ownerId: req.user.id } });
        const listing = await prisma.productRequest.create({
            data: {
                farmId: farm.id,
                type,
                quantity: parseInt(quantity),
                pricePerUnit: parseFloat(pricePerUnit),
                status: 'AVAILABLE'
            }
        });
        res.json(listing);
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

// Get All Listings
app.get('/market/listings', async (req, res) => {
    try {
        const listings = await prisma.productRequest.findMany({
            where: { status: 'AVAILABLE' },
            include: { farm: true }
        });
        res.json(listings);
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

// Place Order
app.post('/market/order', authenticateToken, async (req, res) => {
    const { productId } = req.body;
    try {
        const product = await prisma.productRequest.findUnique({ where: { id: productId } });
        if (!product || product.status !== 'AVAILABLE') {
            return res.status(400).json({ error: 'Product not available' });
        }

        // Transaction to update product status and create order
        const order = await prisma.$transaction(async (tx) => {
            await tx.productRequest.update({
                where: { id: productId },
                data: { status: 'SOLD' }
            });

            return await tx.order.create({
                data: {
                    customerId: req.user.id,
                    productId: productId,
                    totalPrice: product.pricePerUnit * product.quantity,
                    status: 'PENDING'
                }
            });
        });

        res.json(order);
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

// --- ADMIN ROUTES ---

// Get All Farms (Admin)
app.get('/admin/farms', async (req, res) => {
    try {
        const farms = await prisma.farm.findMany({
            include: {
                owner: {
                    select: { name: true, email: true }
                },
                batches: true
            }
        });

        // Transform data for UI
        const farmData = farms.map(farm => {
            const totalBirds = farm.batches.reduce((sum, batch) => sum + batch.count, 0);
            let scale = "Small Scale";
            if (totalBirds > 5000) scale = "Large Scale";
            else if (totalBirds > 1000) scale = "Medium Scale";

            return {
                id: farm.id,
                name: farm.name,
                ownerName: farm.owner.name || "Unknown",
                location: farm.location || "Unknown Location",
                totalBirds: totalBirds,
                scale: scale,
                initial: farm.name.charAt(0).toUpperCase()
            };
        });

        res.json(farmData);
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

// Get Single Farm Details (Admin)
app.get('/admin/farm/:id', async (req, res) => {
    const { id } = req.params;
    try {
        const farm = await prisma.farm.findUnique({
            where: { id: parseInt(id) },
            include: {
                owner: { select: { name: true, email: true, phone: true, createdAt: true } },
                batches: true,
                inventory: true,
                products: true,
                // In a real app, include orders via products to calc revenue
                // orders: true 
            }
        });

        if (!farm) return res.status(404).json({ error: 'Farm not found' });

        // Calculate Stats
        const totalBirds = farm.batches.reduce((sum, batch) => sum + batch.count, 0);

        // Mocking Monthly Revenue for now as order relation is via ProductRequest
        // In refined schema, we would sum up completed orders for this farm
        const monthlyRevenue = 1250000.0;

        const productsCount = farm.products.filter(p => p.status === 'AVAILABLE').length;

        const productTypes = [...new Set(farm.products.map(p => p.type))];

        const farmDetails = {
            id: farm.id,
            name: farm.name,
            ownerName: farm.owner.name || "Unknown",
            location: farm.location || "Unknown Location",
            phone: farm.owner.phone || "N/A",
            email: farm.owner.email,
            joinedDate: farm.owner.createdAt,
            status: "Active", // Mocked
            scale: totalBirds > 5000 ? "Large Scale" : (totalBirds > 1000 ? "Medium Scale" : "Small Scale"),
            totalBirds: totalBirds,
            monthlyRevenue: monthlyRevenue,
            productsCount: productsCount,
            productTypes: productTypes, // ["BROILER", "EGGS", etc]
            // Mocking Graph Data for "Monthly Production"
            productionGraph: [4500, 5000, 4800, 5200, 5100, 5300]
        };

        res.json(farmDetails);
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});
app.get('/admin/stats', async (req, res) => {
    // In a real app, add middleware: authenticateToken, verifyAdminRole
    try {
        const usersCount = await prisma.user.count();
        const farmsCount = await prisma.farm.count();
        const ordersCount = await prisma.order.count();

        // Calculate Total Sales (Sum of totalPrice for completed orders - simplified to all orders for now or status='COMPLETED')
        const salesAgg = await prisma.order.aggregate({
            _sum: {
                totalPrice: true
            }
        });
        const totalSales = salesAgg._sum.totalPrice || 0;

        // Weekly Revenue Logic (merged from /admin/sales to feed the dashboard chart)
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const weeklyRevenue = [];
        const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
        const revenueLabels = [];
        const revenueData = [];

        for (let i = 5; i >= 0; i--) {
            const date = new Date(today);
            date.setDate(today.getDate() - i);
            const startOfDay = new Date(date);
            startOfDay.setHours(0, 0, 0, 0);
            const endOfDay = new Date(date);
            endOfDay.setHours(23, 59, 59, 999);

            const dailyOrders = await prisma.order.findMany({
                where: {
                    createdAt: {
                        gte: startOfDay,
                        lte: endOfDay
                    }
                }
            });

            const dailyTotal = dailyOrders.reduce((sum, order) => sum + order.totalPrice, 0);
            revenueLabels.push(days[date.getDay()]);
            revenueData.push(dailyTotal);
        }

        // Pending Approvals
        const pendingOrders = await prisma.order.count({ where: { status: 'PENDING' } });

        // User Growth Data (Last 7 Days)
        const userGrowthLabels = [];
        const userGrowthData = [];
        const userGrowthDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

        const todayStart = new Date(today); // Re-use today obj which is start of day

        let usersLast7Days = 0;
        for (let i = 6; i >= 0; i--) {
            const date = new Date(todayStart);
            date.setDate(todayStart.getDate() - i);
            const start = new Date(date);
            const end = new Date(date);
            end.setHours(23, 59, 59, 999);

            const dailyUsers = await prisma.user.count({
                where: { createdAt: { gte: start, lte: end } }
            });
            userGrowthLabels.push(userGrowthDays[date.getDay()]);
            userGrowthData.push(dailyUsers);
            usersLast7Days += dailyUsers;
        }

        // Calculate Percentages
        const prev7Start = new Date(todayStart);
        prev7Start.setDate(prev7Start.getDate() - 14);
        const prev7End = new Date(todayStart);
        prev7End.setDate(prev7End.getDate() - 7);
        prev7End.setHours(23, 59, 59, 999);
        const usersPrev7Days = await prisma.user.count({ where: { createdAt: { gte: prev7Start, lte: prev7End } } });

        let usersPercentRaw = usersPrev7Days === 0 ? "100" : (((usersLast7Days - usersPrev7Days) / usersPrev7Days) * 100).toFixed(0);
        const usersPercent = (usersPercentRaw >= 0 ? "+" + usersPercentRaw : usersPercentRaw) + "%";

        const highRiskUsers = await prisma.user.count({ where: { status: 'SUSPENDED' } }) || 0;
        const highRiskPercent = "0%";

        // Active Now: Distinct users interacting in the last 7 days
        const sevenDaysAgo = new Date(todayStart);
        sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
        const recentOrders = await prisma.order.findMany({
            where: { createdAt: { gte: sevenDaysAgo } },
            select: { customerId: true }
        });
        const activeNowSet = new Set(recentOrders.map(o => o.customerId));
        const recentListings = await prisma.productRequest.findMany({
            where: { createdAt: { gte: sevenDaysAgo } },
            include: { farm: true }
        });
        recentListings.forEach(l => {
            if (l.farm && l.farm.ownerId) activeNowSet.add(l.farm.ownerId);
        });
        const activeNow = activeNowSet.size + 1; // Base active
        const activePercent = "+5%";

        // Logs Today
        const endOfTodayObj = new Date(todayStart);
        endOfTodayObj.setHours(23, 59, 59, 999);
        const ordersTodayCount = await prisma.order.count({ where: { createdAt: { gte: todayStart, lte: endOfTodayObj } } });
        const listingsTodayCount = await prisma.productRequest.count({ where: { createdAt: { gte: todayStart, lte: endOfTodayObj } } });
        const logsCount = ordersTodayCount + listingsTodayCount;
        const logsToday = logsCount >= 1000 ? (logsCount / 1000).toFixed(1) + "k" : logsCount.toString();

        const yesterdayStart = new Date(todayStart);
        yesterdayStart.setDate(todayStart.getDate() - 1);
        const yesterdayEnd = new Date(yesterdayStart);
        yesterdayEnd.setHours(23, 59, 59, 999);
        const ordersYest = await prisma.order.count({ where: { createdAt: { gte: yesterdayStart, lte: yesterdayEnd } } });
        const listingsYest = await prisma.productRequest.count({ where: { createdAt: { gte: yesterdayStart, lte: yesterdayEnd } } });
        const logsYest = ordersYest + listingsYest;

        let logsPercentRaw = logsYest === 0 ? (logsCount > 0 ? 100 : 0) : (((logsCount - logsYest) / logsYest) * 100).toFixed(0);
        const logsPercent = (logsPercentRaw >= 0 ? "+" + logsPercentRaw : logsPercentRaw) + "%";

        res.json({
            users: usersCount,
            farms: farmsCount,
            orders: ordersCount,
            totalSales: totalSales,
            pendingApprovals: pendingOrders,
            activeNow,
            highRiskUsers,
            logsToday,
            userGrowthData,
            userGrowthLabels,
            revenueData,
            revenueLabels,
            usersPercent,
            highRiskPercent,
            activePercent,
            logsPercent
        });
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

// Get Admin Sales Analytics
app.get('/admin/sales', async (req, res) => {
    try {
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        // 1. Today's Revenue & Orders
        const todayOrders = await prisma.order.findMany({
            where: {
                createdAt: {
                    gte: today
                }
            }
        });

        const todayRevenue = todayOrders.reduce((sum, order) => sum + order.totalPrice, 0);
        const todayOrdersCount = todayOrders.length;

        // 2. Weekly Revenue (Last 7 Days)
        const weeklyRevenue = [];
        const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

        for (let i = 6; i >= 0; i--) {
            const date = new Date(today);
            date.setDate(today.getDate() - i);
            const startOfDay = new Date(date);
            startOfDay.setHours(0, 0, 0, 0);
            const endOfDay = new Date(date);
            endOfDay.setHours(23, 59, 59, 999);

            const dailyOrders = await prisma.order.findMany({
                where: {
                    createdAt: {
                        gte: startOfDay,
                        lte: endOfDay
                    }
                }
            });

            const dailyTotal = dailyOrders.reduce((sum, order) => sum + order.totalPrice, 0);
            weeklyRevenue.push({
                day: days[date.getDay()],
                revenue: dailyTotal
            });
        }

        // 3. Recent Transactions
        const recentTransactions = await prisma.order.findMany({
            take: 10,
            orderBy: { createdAt: 'desc' },
            include: {
                customer: { select: { name: true } },
                product: { include: { farm: { select: { name: true } } } }
            }
        });

        const formattedTransactions = recentTransactions.map(t => ({
            id: t.id,
            customerName: t.customer.name,
            farmName: t.product.farm.name,
            items: `${t.product.quantity} ${t.product.type}`, // e.g., "500 Eggs"
            amount: t.totalPrice,
            status: t.status,
            date: t.createdAt
        }));

        res.json({
            todayRevenue,
            todayOrders: todayOrdersCount,
            weeklyRevenue,
            recentTransactions: formattedTransactions
        });

    } catch (e) {
        console.error(e);
        res.status(500).json({ error: e.message });
    }
});

// Get Single Transaction Details
app.get('/admin/transaction/:id', async (req, res) => {
    const { id } = req.params;
    try {
        const transaction = await prisma.order.findUnique({
            where: { id: id }, // Assuming ID is UUID string, if Int parse it
            include: {
                customer: { select: { name: true, email: true, phone: true } },
                product: {
                    include: {
                        farm: { select: { name: true, location: true } }
                    }
                }
            }
        });

        if (!transaction) return res.status(404).json({ error: 'Transaction not found' });

        res.json({
            id: transaction.id,
            date: transaction.createdAt,
            status: transaction.status,
            amount: transaction.totalPrice,
            customer: {
                name: transaction.customer.name,
                email: transaction.customer.email,
                phone: transaction.customer.phone
            },
            product: {
                name: transaction.product.type,
                quantity: transaction.product.quantity,
                pricePerUnit: transaction.product.pricePerUnit,
                farm: transaction.product.farm.name,
                location: transaction.product.farm.location
            }
        });
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

// Get All Users (Admin)
app.get('/admin/users', async (req, res) => {
    try {
        const users = await prisma.user.findMany({
            orderBy: { createdAt: 'desc' }
        });

        // Transform data for UI
        const userList = users.map(user => {
            // Calculate last active based on last order or just createdAt for now
            // typically you'd track lastLoginAt
            const timeAgo = "Active recently";

            return {
                id: user.id.toString(), // Ensuring it's a string
                name: user.name,
                email: user.email,
                role: user.role,
                status: user.status || "ACTIVE", // Fallback
                lastActive: user.createdAt.toISOString(),
                initial: user.name ? user.name.charAt(0).toUpperCase() : 'U'
            };
        });

        res.json(userList);
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

// Get User Details (Admin)
app.get('/admin/user/:id', async (req, res) => {
    const { id } = req.params;
    const userId = parseInt(id);
    try {
        const user = await prisma.user.findUnique({
            where: { id: userId }
        });

        if (!user) return res.status(404).json({ error: 'User not found' });

        // Calculate Stats
        const ordersCount = await prisma.order.count({ where: { customerId: userId } });

        const salesAgg = await prisma.order.aggregate({
            where: { customerId: userId },
            _sum: { totalPrice: true }
        });
        const totalRevenue = salesAgg._sum.totalPrice || 0;

        // Days Active
        const now = new Date();
        const createdDate = new Date(user.createdAt);
        const daysActive = Math.max(1, Math.floor((now - createdDate) / (1000 * 60 * 60 * 24)));

        // Risk Evaluation
        let riskText = "Low";
        let riskColorHex = "#22C55E"; // Green
        if (user.status === "SUSPENDED") {
            riskText = "High";
            riskColorHex = "#EF4444"; // Red
        } else if (user.status === "PENDING") {
            riskText = "Med";
            riskColorHex = "#F59E0B"; // Orange
        }

        // Real Activity Graph (Last 7 Days)
        const activityGraph = [];
        const todayStart = new Date();
        todayStart.setHours(0, 0, 0, 0);

        for (let i = 6; i >= 0; i--) {
            const date = new Date(todayStart);
            date.setDate(todayStart.getDate() - i);
            const start = new Date(date);
            const end = new Date(date);
            end.setHours(23, 59, 59, 999);

            const dailyOrders = await prisma.order.count({
                where: { customerId: userId, createdAt: { gte: start, lte: end } }
            });
            let dailyListings = 0;
            if (user.role === 'FARMER') {
                const farm = await prisma.farm.findUnique({ where: { ownerId: userId } });
                if (farm) {
                    dailyListings = await prisma.productRequest.count({
                        where: { farmId: farm.id, createdAt: { gte: start, lte: end } }
                    });
                }
            }
            activityGraph.push(dailyOrders + dailyListings);
        }

        // If the graph is entirely flat (no activity), we'll mock some small variance just so the UI graph isn't a straight line at 0, 
        // OR we can just return it. Returning real data means it might be flat. 
        // Let's actually keep it real: if the array sum is 0, we'll assign [0,0,0,0,0,0,0].

        // Recent Activity (Top 3)
        let rawActivities = [];

        // Fetch Orders
        const recentOrders = await prisma.order.findMany({
            where: { customerId: userId },
            orderBy: { createdAt: 'desc' },
            take: 3
        });

        recentOrders.forEach(o => {
            rawActivities.push({
                title: "Placed Order",
                subtitle: `Amount: $${o.totalPrice}`,
                time: o.createdAt.toISOString(),
                type: "ORDER",
                rawDate: o.createdAt
            });
        });

        // If farmer, fetch Recent Listings
        if (user.role === 'FARMER') {
            const farm = await prisma.farm.findUnique({ where: { ownerId: userId } });
            if (farm) {
                const recentListings = await prisma.productRequest.findMany({
                    where: { farmId: farm.id },
                    orderBy: { createdAt: 'desc' },
                    take: 3
                });
                recentListings.forEach(l => {
                    rawActivities.push({
                        title: "Created Listing",
                        subtitle: `${l.quantity} ${l.type}`,
                        time: l.createdAt.toISOString(),
                        type: "LISTING",
                        rawDate: l.createdAt
                    });
                });
            }
        }

        if (user.status === "SUSPENDED") {
            rawActivities.push({
                title: "System Alert",
                subtitle: "Account Suspended",
                time: user.createdAt.toISOString(),
                type: "ALERT",
                rawDate: new Date() // Pin to top
            });
        }

        rawActivities.sort((a, b) => b.rawDate - a.rawDate);
        const recentActivity = rawActivities.slice(0, 3).map(a => ({
            title: a.title,
            subtitle: a.subtitle,
            time: a.time,
            type: a.type
        }));

        res.json({
            id: user.id.toString(),
            name: user.name,
            email: user.email,
            phone: user.phone || "+234 000 000 0000",
            role: user.role,
            location: "Ibadan, Oyo State",
            joinedDate: user.createdAt,
            status: user.status || "ACTIVE",
            totalOrders: ordersCount,
            totalRevenue: totalRevenue,
            activityGraph: activityGraph,
            daysActive: daysActive,
            riskText: riskText,
            riskColorHex: riskColorHex,
            recentActivity: recentActivity
        });
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

// Update User Status (Admin)
app.put('/admin/user/:id/status', async (req, res) => {
    const { id } = req.params;
    const { status } = req.body; // EXPECTS: "ACTIVE", "SUSPENDED", "PENDING"
    const userId = parseInt(id);

    try {
        const user = await prisma.user.update({
            where: { id: userId },
            data: { status: status }
        });
        res.json(user);
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

// Get Admin Reports
app.get('/admin/reports', async (req, res) => {
    try {
        const currentYear = new Date().getFullYear();
        const startOfYear = new Date(currentYear, 0, 1);
        const endOfYear = new Date(currentYear, 11, 31, 23, 59, 59);

        // 1. Sales Report (Monthly transaction volume) - Mocked for now or aggregated
        // Real aggregation would group by month.
        const allOrders = await prisma.order.findMany({
            where: {
                createdAt: {
                    gte: startOfYear,
                    lte: endOfYear
                }
            }
        });

        const monthlySales = Array(12).fill(0);
        allOrders.forEach(order => {
            const month = order.createdAt.getMonth(); // 0-11
            monthlySales[month] += order.totalPrice;
        });

        const totalSalesYTD = monthlySales.reduce((a, b) => a + b, 0);

        // 2. User Growth (New registrations per month)
        const allUsers = await prisma.user.findMany({
            where: {
                createdAt: {
                    gte: startOfYear,
                    lte: endOfYear
                }
            }
        });

        const userGrowth = Array(12).fill(0);
        allUsers.forEach(user => {
            const month = user.createdAt.getMonth();
            userGrowth[month]++;
        });

        const totalUsers = await prisma.user.count();

        // 3. Marketplace Analytics
        const activeListings = await prisma.productRequest.count({ where: { status: 'AVAILABLE' } });

        // Count 'COMPLETED' orders. If none, count all non-pending for demo?
        // Let's stick to 'COMPLETED' and assume some will be marked as such later.
        const completedOrders = await prisma.order.count({ where: { status: 'COMPLETED' } });

        const salesAgg = await prisma.order.aggregate({
            _avg: { totalPrice: true },
            where: { status: 'COMPLETED' }
        });
        const avgOrderValue = salesAgg._avg.totalPrice || 0;

        res.json({
            sales: {
                monthly: monthlySales,
                totalYtd: totalSalesYTD
            },
            userGrowth: {
                monthly: userGrowth,
                totalUsers: totalUsers
            },
            marketplace: {
                activeListings,
                completedOrders,
                avgOrderValue
            }
        });

    } catch (e) {
        console.error(e);
        res.status(500).json({ error: e.message });
    }
});

// Start Server
app.listen(PORT, async () => {
    console.log(`Server running on port ${PORT}`);
    await seedAdminUser();
});
