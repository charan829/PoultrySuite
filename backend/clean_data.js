const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

async function cleanData() {
    try {
        console.log('Cleaning database...');

        const adminEmail = 'admin@gmail.com';

        // 1. Delete Orders
        await prisma.order.deleteMany({});
        console.log('Deleted all Orders');

        // 2. Delete ProductRequests (Marketplace Listings)
        await prisma.productRequest.deleteMany({});
        console.log('Deleted all ProductRequests');

        // 3. Delete Expenses
        await prisma.expense.deleteMany({});
        console.log('Deleted all Expenses');

        // 4. Delete Inventory
        await prisma.inventory.deleteMany({});
        console.log('Deleted all Inventory');

        // 5. Delete Batches
        await prisma.batch.deleteMany({});
        console.log('Deleted all Batches');

        // 6. Delete Farms. 
        // We need to keep farms owned by the admin (if any), although admin usually doesn't own a farm in this app context.
        // But to be safe, let's find the admin user first.
        const admin = await prisma.user.findUnique({
            where: { email: adminEmail },
        });

        if (!admin) {
            console.log("Admin user not found! Aborting to prevent total data loss.");
            return;
        }

        // Delete farms NOT owned by admin
        await prisma.farm.deleteMany({
            where: {
                ownerId: {
                    not: admin.id
                }
            }
        });
        console.log('Deleted Farms (except Admin\'s)');

        // 7. Delete Users (except Admin)
        await prisma.user.deleteMany({
            where: {
                email: {
                    not: adminEmail
                }
            }
        });
        console.log('Deleted Users (except Admin)');

        console.log('Database cleanup complete.');

    } catch (error) {
        console.error('Error cleaning data:', error);
    } finally {
        await prisma.$disconnect();
    }
}

cleanData();
