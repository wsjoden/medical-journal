import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from "../services/AuthContext";
import '../styles/DashboardPage.css';

function DashboardPage() {
    const { role } = useAuth();
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (role !== null) {
            setLoading(false);
        }
    }, [role]);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (role !== 'doctor' && role !== 'other_staff') {
        return <div>Access Denied</div>;
    }

    return (
        <div className="dashboard-page">
            <h1>Dashboard</h1>
            <div className="dashboard-content">
                <div className="card">
                    <Link to="/patient" className="card-link">
                        <h2>Patients</h2>
                        <p>Patients Overview</p>
                    </Link>
                </div>
                <div className="card">
                    <h2>Observations</h2>
                    <p>Latest Observations</p>
                </div>
                <div className="card">
                    <Link to="/inbox" className="card-link">
                        <h2>Messages</h2>
                        <p>Latest Messages</p>
                    </Link>
                </div>
            </div>
        </div>
    );
}

export default DashboardPage;
