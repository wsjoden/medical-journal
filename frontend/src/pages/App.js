/**
 * App.jsx - Main Application Entry Point
 * 
 * Root application file that sets up routing and authentication.
 * Defines all routes with appropriate role-based access control.
 * 
 * Security:
 * Uses AuthProvider context to wrap entire app, providing auth state to all components.
 * ProtectedRoute component enforces role-based access control on sensitive routes.
 */

import '../styles/App.css';
import React from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { AuthProvider } from "../services/AuthContext";
import NavBar from "../components/NavBar";
import Home from "./HomePage";
import PatientPage from "./PatientPage";
import DashboardPage from "./DashboardPage";
import PatientDetailsPage from "./PatientDetailsPage";
import NewObservationPage from "./NewObservationPage";
import NewEncounterPage from "./NewEncounterPage";
import NewDiagnosePage from "./NewDiagnosePage";
import ProfilePage from "./ProfilePage";
import ProtectedRoute from '../components/ProtectedRoute';
import InboxPage from "./message/InboxPage";
import ConversationPage from "./message/ConversationPage";
import NewMessagePage from "./message/NewMessagePage";
import ImageUploadPage from "./image/ImageUploadPage";
import ImageEditPage from "./image/ImageEditPage";
import UploadedImagesList from "./image/ImagesListPage";

function App() {
    return (
        <Router>
            <AuthProvider>
                <div className="App">
                    <NavBar />
                    <main className="container mt-5">
                        <Routes>
                            <Route path="/" element={<Home />} />
                            <Route path="/patient" element={<PatientPage />} />

                            {/*Logged on users only */}
                            <Route element={<ProtectedRoute />}>
                                <Route path="/image/upload" element={<ImageUploadPage />} />
                                <Route path="/profile" element={<ProfilePage />} />
                            </Route>

                            {/* Doctors only */}
                            <Route element={<ProtectedRoute allowedRoles={['doctor']} />}>
                                <Route path="/image/edit" element={<ImageEditPage />} />
                                <Route path="/image/list" element={<UploadedImagesList />} />
                            </Route>

                            {/* Doctors and Other Staff only */}
                            <Route element={<ProtectedRoute allowedRoles={['doctor', 'other_staff']} />}>
                                <Route path="/dashboard" element={<DashboardPage />} />
                                <Route path="/observation/new/patient/:id" element={<NewObservationPage />} />
                                <Route path="/encounter/new/patient/:id" element={<NewEncounterPage />} />
                                <Route path="/diagnose/new/patient/:id" element={<NewDiagnosePage />} />
                            </Route>

                            <Route path="/patient/:id" element={<PatientDetailsPage />} />
                            <Route path="/inbox" element={<InboxPage />} />
                            <Route path="/message/new" element={<NewMessagePage />} />
                            <Route path="/conversation/:id" element={<ConversationPage />} />
                        </Routes>
                    </main>
                </div>
            </AuthProvider>
        </Router>
    );
}

export default App;