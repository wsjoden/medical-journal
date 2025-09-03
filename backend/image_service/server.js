require('dotenv').config();
const express = require("express");
const multer = require("multer");
const cors = require("cors");
const fs = require("fs");
const path = require("path");
const jwt = require("jsonwebtoken");
const jwksClient = require("jwks-rsa");
const { sign } = require('crypto');

//Keycloak Config
const KEYCLOAK_REALM_URL = process.env.KEYCLOAK_REALM_URL;
const JWKS_URI = `${KEYCLOAK_REALM_URL}/protocol/openid-connect/certs`;

// JWKS client to retrieve Keycloak public key
const client = jwksClient({
    jwksUri: JWKS_URI,
    requestHeaders: {},
    timeout: 30000
});

function getKey(header, callback) {
    client.getSigningKey(header.kid, (e, key) => {
        if (e) {
            console.error('error retrieving key', e);
            return callback(e);
        }
        const signingKey = key.getPublicKey();
        callback(null, signingKey);
    });
}

// Middleware to verify JWT tokens
const verifyJwt = (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ message: "Access denied. no token." })
    }

    const token = authHeader.split(' ')[1];
    jwt.verify(token, getKey, {
        audience: 'account',
        issuer: KEYCLOAK_REALM_URL,
        algorithms: ['RS256']
    }, (e, decoded) => {
        if (e) {
            console.error('JWT verification failed: ', e);
            return res.status(401).json({ message: "Access denied. invalid token." })
        }
        req.auth = decoded;
        console.log(`Authenticated user: ${decoded.preferred_username}, Role: ${decoded.role}`);
        next();
    });
};

function checkRole(allowedRoles) {
    return (req, res, next) => {
        if (!req.auth || !req.auth.role) {
            return res.status(403).json({ message: "Access denied. No role found." });
        }
        const userRole = req.auth.role.toLowerCase(); // Extract user's role from JWT
        const normalizedAllowedRoles = allowedRoles.map(role => role.toLowerCase());

        if (!normalizedAllowedRoles.includes(userRole)) {
            return res.status(403).json({
                message: `Access denied. Required roles: ${allowedRoles.join(", ")}. Your role: ${req.auth.role}`
            });
        }

        next();
    };
}

// Multer config
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        const uploadsDir = 'uploads/';
        if (!fs.existsSync(uploadsDir)) {
            fs.mkdirSync(uploadsDir, { recursive: true });
        }
        cb(null, uploadsDir);
    },
    filename: (req, file, cb) => {
        // Sanitize filename to prevent path traversal
        const sanitizedName = file.originalname.replace(/[^a-zA-Z0-9.-]/g, '_');
        cb(null, `${Date.now()}-${sanitizedName}`);
    },
});

const upload = multer({ storage });
const app = express();

// CORS configuration
app.use(cors({
    origin: [
        'https://medical-app-frontend.app.cloud.cbh.kth.se',
        'http://localhost:3000'
    ],
    credentials: true
}));

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Endpoints
app.get("/images/test", verifyJwt, checkRole(["Doctor", "Other_Staff", "Patient"]), (req, res) => {
    res.send("Image service is up and running!");
});

//
app.post("/images/upload", verifyJwt, checkRole(["Patient", "Doctor", "Other_Staff"]), upload.single("file"), uploadFiles);

function uploadFiles(req, res) {
    console.log('Upload request from:', req.auth.preferred_username);
    console.log('Uploaded file:', req.file);

    if (!req.file) {
        return res.status(400).json({ message: "No file uploaded" });
    }

    const uploadsDataPath = path.join(__dirname, 'uploads', 'uploadsData.json');
    let uploadsData = [];

    if (fs.existsSync(uploadsDataPath)) {
        try {
            const data = fs.readFileSync(uploadsDataPath);
            uploadsData = JSON.parse(data);
        } catch (error) {
            console.error('Error reading uploads data:', error);
            uploadsData = [];
        }
    }

    const fileData = {
        name: req.body.name,
        filename: req.file.filename,
        originalname: req.file.originalname,
        path: req.file.path,
        mimetype: req.file.mimetype,
        size: req.file.size,
        uploadedBy: req.auth.preferred_username,
        uploadedAt: new Date().toISOString(),
        userId: req.auth.sub
    };

    uploadsData.push(fileData);

    try {
        fs.writeFileSync(uploadsDataPath, JSON.stringify(uploadsData, null, 2));
        res.json({
            message: "File uploaded successfully.",
            file: {
                name: fileData.name,
                mimetype: fileData.mimetype,
                size: fileData.size
            }
        });
    } catch (error) {
        console.error('Error saving uploads data:', error);
        res.status(500).json({ message: "Error saving file data" });
    }
}

app.get("/images/list", verifyJwt, checkRole(["Doctor", "Other_Staff"]), (req, res) => {
    const uploadsDataPath = path.join(__dirname, 'uploads', 'uploadsData.json');

    if (fs.existsSync(uploadsDataPath)) {
        try {
            const rawData = fs.readFileSync(uploadsDataPath);
            const uploadsData = JSON.parse(rawData);
            res.json(uploadsData);
        } catch (error) {
            console.error('Error reading uploads data:', error);
            res.status(500).json({ message: "Error reading file data" });
        }
    } else {
        res.json([]);
    }
});

// Start Server
const PORT = process.env.PORT || 8085;
app.listen(PORT, () => {
    console.log(`Server v6 started on port ${PORT}...`);
});

module.exports = app;