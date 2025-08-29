import React, { useEffect } from 'react';
import '../../styles/ImageUploadPage.css';

const ImageUploadPage = () => {
    useEffect(() => {
        const form = document.getElementById("form");

        form.addEventListener("submit", submitForm);

        async function submitForm(e) {
            e.preventDefault();
            const name = document.getElementById("name");
            const fileInput = document.getElementById("file");
            const formData = new FormData();
            formData.append("name", name.value);
            formData.append("file", fileInput.files[0]); // Append single file

            // Log the file details to the console
            console.log('File to be uploaded:', fileInput.files[0]);

            try {
                const response = await fetch("http://localhost:8085/images/upload", {
                    method: 'POST',
                    body: formData,
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('token')}`
                    }
                });

                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }

                const data = await response.json();
                console.log('File uploaded successfully:', data);
            } catch (error) {
                console.error('Error occurred:', error);
            }
        }

        // Cleanup event listener on component unmount
        return () => {
            form.removeEventListener("submit", submitForm);
        };
    }, []);

    return (
        <div className="container">
            <h1>File Upload</h1>
            <form id='form'>
                <div className="input-group">
                    <label htmlFor='name'>Your name</label>
                    <input name='name' id='name' placeholder="Enter your name" />
                </div>
                <div className="input-group">
                    <label htmlFor='file'>Select file</label>
                    <input id='file' type="file" />
                </div>
                <button className="submit-btn" type='submit'>Upload</button>
            </form>
        </div>
    );
};

export default ImageUploadPage;