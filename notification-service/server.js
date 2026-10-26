// =========================================================================
// Notification Service - Buoi thuc hanh 2 (Bai 3: Framework khac - Node.js)
// Port: 8083
// Vai tro: Service Consumer goi sang Spring Boot Booking Service (Port 8081)
// =========================================================================

const http = require('http');
const url = require('url');
const fs = require('fs');
const path = require('path');

// Nạp cấu hình từ file .env tổng ở thư mục gốc
function loadMasterEnv() {
    const envPaths = [
        path.resolve(__dirname, '../.env'),
        path.resolve(__dirname, '.env')
    ];
    for (const p of envPaths) {
        if (fs.existsSync(p)) {
            const content = fs.readFileSync(p, 'utf8');
            content.split(/\r?\n/).forEach(line => {
                const trimmed = line.trim();
                if (trimmed && !trimmed.startsWith('#')) {
                    const idx = trimmed.indexOf('=');
                    if (idx !== -1) {
                        const key = trimmed.substring(0, idx).trim();
                        const val = trimmed.substring(idx + 1).trim();
                        if (!process.env[key]) {
                            process.env[key] = val;
                        }
                    }
                }
            });
            console.log(`[Notification Service] Da nap cau hinh tu file .env: ${p}`);
            break;
        }
    }
}
loadMasterEnv();

const PORT = parseInt(process.env.NOTIFICATION_SERVICE_PORT, 10) || 8083;
const BOOKING_SERVICE_URL = process.env.BOOKING_SERVICE_URL || 'http://localhost:8081';

// Luu tru thong bao trong RAM (In-memory)
const notifications = [
    {
        id: 1,
        bookingId: 1,
        recipient: 'Nguyen Van An (0901234567)',
        message: 'Lich dat San so 1 vao 18:00 ngay 2026-09-25 da duoc xac nhan.',
        timestamp: new Date().toISOString()
    }
];

function sendJson(res, statusCode, data) {
    res.writeHead(statusCode, { 'Content-Type': 'application/json; charset=utf-8' });
    res.end(JSON.stringify(data, null, 2));
}

const server = http.createServer(async (req, res) => {
    // Enable CORS
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    const parsedUrl = url.parse(req.url, true);
    const pathname = parsedUrl.pathname;

    // 1. GET /api/notifications/info
    if (req.method === 'GET' && pathname === '/api/notifications/info') {
        return sendJson(res, 200, {
            service: 'Notification Service',
            framework: 'Node.js (Vanilla HTTP / Express compatible)',
            version: '1.0.0',
            port: PORT,
            role: 'Consumer - Consumes Spring Boot REST API',
            status: 'UP'
        });
    }

    // 2. GET /api/notifications
    if (req.method === 'GET' && pathname === '/api/notifications') {
        return sendJson(res, 200, notifications);
    }

    // 3. GET /api/notifications/verify-booking/:id
    // Service-to-Service: Node.js goi sang Spring Boot tai port 8081
    const verifyMatch = pathname.match(/^\/api\/notifications\/verify-booking\/(\d+)$/);
    if (req.method === 'GET' && verifyMatch) {
        const bookingId = verifyMatch[1];
        try {
            // Su dung fetch cua Node.js de goi sang Spring Boot
            const targetUrl = `${BOOKING_SERVICE_URL}/api/bookings/${bookingId}`;
            console.log(`[Node.js -> Spring Boot] Calling GET ${targetUrl}`);

            const response = await fetch(targetUrl);
            if (!response.ok) {
                if (response.status === 404) {
                    return sendJson(res, 404, {
                        status: 404,
                        error: 'Not Found',
                        message: `Booking id=${bookingId} khong ton tai tren Spring Boot Booking Service.`
                    });
                }
                return sendJson(res, response.status, {
                    status: response.status,
                    message: `Loi tu Booking Service: ${response.statusText}`
                });
            }

            const bookingData = await response.json();
            return sendJson(res, 200, {
                service: 'Notification Service (Node.js)',
                communication: 'HTTP GET -> Spring Boot (Port 8081)',
                success: true,
                verifiedBooking: bookingData,
                notificationPreview: `Gui SMS den ${bookingData.phone}: Ban da dat thanh cong san ${bookingData.fieldName} ngay ${bookingData.bookingDate} luc ${bookingData.startTime}!`
            });
        } catch (error) {
            console.error('[Error calling Booking Service]:', error.message);
            return sendJson(res, 503, {
                status: 503,
                error: 'Service Unavailable',
                message: 'Khong the ket noi toi Spring Boot Booking Service (Port 8081). Dịch vu co the dang tat.'
            });
        }
    }

    // 4. POST /api/notifications (Nhan event tao thong bao)
    if (req.method === 'POST' && pathname === '/api/notifications') {
        let body = '';
        req.on('data', chunk => { body += chunk; });
        req.on('end', () => {
            try {
                const data = JSON.parse(body);
                const newNotification = {
                    id: notifications.length + 1,
                    bookingId: data.bookingId,
                    recipient: data.recipient || 'Khach hang',
                    message: data.message || 'Thong bao dat lich san bong da',
                    timestamp: new Date().toISOString()
                };
                notifications.push(newNotification);
                return sendJson(res, 201, {
                    message: 'Da tao thong bao thanh cong (Node.js Consumer)',
                    data: newNotification
                });
            } catch (e) {
                return sendJson(res, 400, { status: 400, message: 'JSON khong hop le' });
            }
        });
        return;
    }

    // 404 Not Found
    sendJson(res, 404, {
        status: 404,
        message: `Endpoint ${pathname} khong ton tai tren Notification Service (Node.js)`
    });
});

server.listen(PORT, () => {
    console.log(`=======================================================`);
    console.log(`[Notification Service] Running on http://localhost:${PORT}`);
    console.log(`[Target Service] Spring Boot Booking Service at ${BOOKING_SERVICE_URL}`);
    console.log(`=======================================================`);
});
