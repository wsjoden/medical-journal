import { useState } from 'react';
import '../../styles/ImageUploadPage.css';
import { apiRequestFile } from '../../services/RESTService';

const ImageUploadPage = () => {

    const [name, setName] = useState(' ');
    const [file, setFile] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [message, setMessage] = useState('');

    const handleSubmit = async (e) => { //what is e?
        e.preventDefault();

        if (!file) {
            setMessage('Please select a file to upload');
            return;
        }

        setUploading(true);
        setMessage('');

        try {
            const formData = new FormData();
            formData.append("name", name);
            formData.append("file", file);

            console.log("file to be uploaded: ", file);

            const response = await apiRequestFile('POST', '/images/upload', formData);
            const data = response.data;
            console.log('File uploaded successfully:', data);
            setMessage('File uploaded successfully!');

            // Reset form
            setName('');
            setFile(null);
            // Reset file input
            document.getElementById('file').value = '';

        } catch (error) {
            console.error('Error occurred:', error);
            setMessage(`Error: ${error.message}`);
        } finally {
            setUploading(false);
        }
    };

    // useEffect(() => {
    //     const form = document.getElementById("form");

    //     form.addEventListener("submit", submitForm);

    //     async function submitForm(e) {
    //         e.preventDefault();
    //         const name = document.getElementById("name");
    //         const fileInput = document.getElementById("file");
    //         const formData = new FormData();
    //         formData.append("name", name.value);
    //         formData.append("file", fileInput.files[0]); // Append single file

    //         // Log the file details to the console
    //         console.log('File to be uploaded:', fileInput.files[0]);

    //         apiRequest('POST', '/images/upload',)

    //         try {
    //             const response = await fetch("http://localhost:8085/images/upload", {
    //                 method: 'POST',
    //                 body: formData,
    //                 headers: {
    //                     'Authorization': `Bearer ${localStorage.getItem('token')}`
    //                 }
    //             });

    //             if (!response.ok) {
    //                 throw new Error('Network response was not ok');
    //             }

    //             const data = await response.json();
    //             console.log('File uploaded successfully:', data);
    //         } catch (error) {
    //             console.error('Error occurred:', error);
    //         }
    //     }

    //     // Cleanup event listener on component unmount
    //     return () => {
    //         form.removeEventListener("submit", submitForm);
    //     };
    // }, []);

    return (
        <div className="container">
            <h1>File Upload</h1>
            <form onSubmit={handleSubmit}>
                <div className="input-group">
                    <label htmlFor='name'>Your name</label>
                    <input
                        name='name'
                        id='name'
                        placeholder="Enter your name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        required
                    />
                </div>
                <div className="input-group">
                    <label htmlFor='file'>Select file</label>
                    <input
                        id='file'
                        type="file"
                        onChange={(e) => setFile(e.target.files[0])}
                        required
                    />
                </div>
                {message && (
                    <div className={`message ${message.includes('Error') ? 'error' : 'success'}`}>
                        {message}
                    </div>
                )}
                <button
                    className="submit-btn"
                    type='submit'
                    disabled={uploading}
                >
                    {uploading ? 'Uploading...' : 'Upload'}
                </button>
            </form>
        </div>
    );
};

export default ImageUploadPage;