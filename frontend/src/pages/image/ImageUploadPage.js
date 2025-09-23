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

            console.log('FormData contents:');
            for (let [key, value] of formData.entries()) {
                console.log(`${key}:`, value);
            }

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