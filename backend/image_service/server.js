require('dotenv').config();
const express = require("express");
const multer = require("multer");
const cors = require("cors");
const fs = require("fs");
const path = require("path");
const jwt = require("jsonwebtoken");

const KEYCLOAK_ISSUER = process.env.KEYCLOAK_ISSUER;
const KEYCLOAK_CLIENT_ID = process.env.KEYCLOAK_CLIENT_ID;

const decodeJwt = (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (authHeader) {
        const token = authHeader.split(' ')[1];
        const decodedToken = jwt.decode(token, { complete: true });
        console.log("Full Decoded Token:", JSON.stringify(decodedToken, null, 2));
        req.auth = decodedToken ? decodedToken.payload : null;
    } else {
        console.log("No authentication data found.");
        req.auth = null;
    }
    next();
};

const logUserRole = (req, res, next) => {
    if (req.auth) {
        console.log(`User Role: ${req.auth.role || "No role found in token"}`);
    } else {
        console.log("No authentication data found.");
    }
    next();
};

const allowedRoles = ["patient", "other_staff", "doctor"];

function checkRole(allowedRoles) {
    return (req, res, next) => {
        if (!req.auth || !req.auth.role) {
            return res.status(403).json({ message: "Access denied. No role found." });
        }

        const userRole = req.auth.role; // Extract user's role from JWT

        if (!allowedRoles.includes(userRole)) {
            return res.status(403).json({ message: `Access denied. Allowed roles: ${allowedRoles.join(", ")}` });
        }

        next();
    };
}

const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, 'uploads/');
    },
    filename: (req, file, cb) => {
        cb(null, `${Date.now()}-${file.originalname}`);
    },
});

const upload = multer({ storage });

const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cors());

app.get("/images/test", decodeJwt, logUserRole, checkRole(["doctor","other_staff"]), (req, res) => {
    res.send("Image service is up and running!");
});

app.post("/images/upload", decodeJwt, logUserRole, checkRole(["patient", "doctor", "other_staff"]), upload.single("file"), uploadFiles);

function uploadFiles(req, res) {
    console.log('Request body:', req.body);
    console.log('Uploaded file:', req.file);

    const uploadsDataPath = path.join(__dirname, 'uploads', 'uploadsData.json');
    let uploadsData = [];

    if (fs.existsSync(uploadsDataPath)) {
        const data = fs.readFileSync(uploadsDataPath);
        uploadsData = JSON.parse(data);
    }

    uploadsData.push({
        name: req.body.name,
        filename: req.file.filename,
        originalname: req.file.originalname,
        path: req.file.path,
        mimetype: req.file.mimetype,
        size: req.file.size
    });

    fs.writeFileSync(uploadsDataPath, JSON.stringify(uploadsData, null, 2));

    res.json({ message: "File uploaded successfully.", file: req.file });
}

app.get("/images/list", decodeJwt, logUserRole, checkRole(["doctor", "other_staff"]), (req, res) => {
    const uploadsDataPath = path.join(__dirname, 'uploads', 'uploadsData.json');

    if (fs.existsSync(uploadsDataPath)) {
        const rawData = fs.readFileSync(uploadsDataPath);
        const uploadsData = JSON.parse(rawData);
        res.send(uploadsData);
    } else {
        res.send([]);
    }
});

// Start Server
const PORT = process.env.PORT || 8085;
app.listen(PORT, () => {
    console.log(`Server v6 started on port ${PORT}...`);
});