// server.test.js
const request = require('supertest');
const express = require('express');
const fs = require('fs');
const path = require('path');
const jwt = require('jsonwebtoken');

// Mock the server dependencies
jest.mock('fs');
jest.mock('path');
jest.mock('jsonwebtoken');

// Create a mock for the upload destination
const mockUploadsDir = path.join(__dirname, 'uploads');
fs.existsSync.mockImplementation((path) => {
    if (path.includes('uploadsData.json')) {
        return false; // First call for checking file
    }
    return true; // For directory checks
});

// Set up test file data
const mockFile = {
    fieldname: 'file',
    originalname: 'test-image.jpg',
    encoding: '7bit',
    mimetype: 'image/jpeg',
    destination: mockUploadsDir,
    filename: '1646579708000-test-image.jpg',
    path: path.join(mockUploadsDir, '1646579708000-test-image.jpg'),
    size: 12345
};

// Jest setup function to initialize the app before tests
let app;

beforeEach(() => {
    // Reset mocks
    jest.clearAllMocks();

    // Mock JWT decode to return different payloads based on the token
    jwt.decode.mockImplementation((token) => {
        if (token === 'doctor_token') {
            return {
                complete: true,
                payload: { role: 'doctor', sub: 'doctor123' }
            };
        } else if (token === 'staff_token') {
            return {
                complete: true,
                payload: { role: 'other_staff', sub: 'staff123' }
            };
        } else if (token === 'patient_token') {
            return {
                complete: true,
                payload: { role: 'patient', sub: 'patient123' }
            };
        } else {
            return null;
        }
    });

    // Create a clean server instance for each test
    app = express();

    // We need to manually recreate the middleware setup from your server
    app.use(express.json());
    app.use(express.urlencoded({ extended: true }));

    // Mock the decodeJwt middleware
    const decodeJwt = (req, res, next) => {
        const authHeader = req.headers.authorization;
        if (authHeader) {
            const token = authHeader.split(' ')[1];
            const decodedToken = jwt.decode(token, { complete: true });
            req.auth = decodedToken ? decodedToken.payload : null;
        } else {
            req.auth = null;
        }
        next();
    };

    // Mock the logUserRole middleware (simplified for testing)
    const logUserRole = (req, res, next) => {
        next();
    };

    // Recreate the checkRole middleware
    const checkRole = (allowedRoles) => {
        return (req, res, next) => {
            if (!req.auth || !req.auth.role) {
                return res.status(403).json({ message: "Access denied. No role found." });
            }

            const userRole = req.auth.role;

            if (!allowedRoles.includes(userRole)) {
                return res.status(403).json({ message: `Access denied. Allowed roles: ${allowedRoles.join(", ")}` });
            }

            next();
        };
    };

    // Set up routes with middlewares
    app.get("/images/test", decodeJwt, logUserRole, checkRole(["doctor", "other_staff"]), (req, res) => {
        res.send("Image service is up and running!");
    });

    // Modified upload endpoint for testing
    app.post("/images/upload", decodeJwt, logUserRole, checkRole(["patient", "doctor", "other_staff"]), (req, res) => {
        // For testing, we just simulate a successful upload
        res.json({
            message: "File uploaded successfully.",
            file: mockFile
        });
    });

    app.get("/images/list", decodeJwt, logUserRole, checkRole(["doctor", "other_staff"]), (req, res) => {
        const mockUploadsData = [
            {
                name: "Test Upload 1",
                filename: "1646579708000-test-image.jpg",
                originalname: "test-image.jpg",
                path: path.join(mockUploadsDir, "1646579708000-test-image.jpg"),
                mimetype: "image/jpeg",
                size: 12345
            }
        ];

        // For the list endpoint, we'll return mock data
        fs.existsSync.mockReturnValueOnce(true);
        fs.readFileSync.mockReturnValueOnce(JSON.stringify(mockUploadsData));

        const uploadsDataPath = path.join(__dirname, 'uploads', 'uploadsData.json');

        if (fs.existsSync(uploadsDataPath)) {
            const rawData = fs.readFileSync(uploadsDataPath);
            const uploadsData = JSON.parse(rawData);
            res.send(uploadsData);
        } else {
            res.send([]);
        }
    });
});

// Test suites
describe('File Upload Service Tests', () => {
    describe('GET /images/test', () => {
        test('should return 200 for doctor role', async () => {
            const response = await request(app)
                .get('/images/test')
                .set('Authorization', 'Bearer doctor_token');

            expect(response.status).toBe(200);
            expect(response.text).toBe('Image service is up and running!');
        });

        test('should return 200 for staff role', async () => {
            const response = await request(app)
                .get('/images/test')
                .set('Authorization', 'Bearer staff_token');

            expect(response.status).toBe(200);
            expect(response.text).toBe('Image service is up and running!');
        });

        test('should return 403 for patient role', async () => {
            const response = await request(app)
                .get('/images/test')
                .set('Authorization', 'Bearer patient_token');

            expect(response.status).toBe(403);
        });

        test('should return 403 for no token', async () => {
            const response = await request(app)
                .get('/images/test');

            expect(response.status).toBe(403);
        });
    });

    describe('POST /images/upload', () => {
        test('should return 200 for doctor role', async () => {
            const response = await request(app)
                .post('/images/upload')
                .set('Authorization', 'Bearer doctor_token')
                .field('name', 'Test Upload');

            expect(response.status).toBe(200);
            expect(response.body.message).toBe('File uploaded successfully.');
            expect(response.body.file.originalname).toBe('test-image.jpg');
        });

        test('should return 200 for patient role', async () => {
            const response = await request(app)
                .post('/images/upload')
                .set('Authorization', 'Bearer patient_token')
                .field('name', 'Test Upload');

            expect(response.status).toBe(200);
            expect(response.body.message).toBe('File uploaded successfully.');
        });

        test('should return 403 for no token', async () => {
            const response = await request(app)
                .post('/images/upload')
                .field('name', 'Test Upload');

            expect(response.status).toBe(403);
        });
    });

    describe('GET /images/list', () => {
        test('should return 200 and file list for doctor role', async () => {
            const response = await request(app)
                .get('/images/list')
                .set('Authorization', 'Bearer doctor_token');

            expect(response.status).toBe(200);
            expect(Array.isArray(response.body)).toBe(true);
            expect(response.body.length).toBe(1);
            expect(response.body[0].name).toBe('Test Upload 1');
        });

        test('should return 200 and file list for staff role', async () => {
            const response = await request(app)
                .get('/images/list')
                .set('Authorization', 'Bearer staff_token');

            expect(response.status).toBe(200);
            expect(Array.isArray(response.body)).toBe(true);
        });

        test('should return 403 for patient role', async () => {
            const response = await request(app)
                .get('/images/list')
                .set('Authorization', 'Bearer patient_token');

            expect(response.status).toBe(403);
        });

        test('should return 403 for no token', async () => {
            const response = await request(app)
                .get('/images/list');

            expect(response.status).toBe(403);
        });
    });
});