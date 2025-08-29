import { library } from '@fortawesome/fontawesome-svg-core';
import {
    faImage,
    faFont,
    faPencil,
    faFilter,
    faTrash,
    faDownload
} from '@fortawesome/free-solid-svg-icons';

import {Image, IText, filters} from 'fabric';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {useEffect, useState} from "react";

library.add(faImage, faFont, faPencil, faFilter, faTrash, faDownload);

const Toolbox = ({ canvas, currentFilter, setCurrentFilter }) => {
    function fileHandler(e) {
        const file = e.target.files[0];
        const reader = new FileReader();

        reader.onload = async (e) => {
            const image = await Image.fromURL(e.target.result);
            image.scale(0.5);
            canvas.add(image);
            canvas.centerObject(image);
            canvas.setActiveObject(image);
        };
        reader.readAsDataURL(file);
        e.target.value = '';
    }

    useEffect(() => {
        if(!canvas ||
            !canvas.getActiveObject() ||
            !canvas.getActiveObject().isType('image')) return;

        function getSelectedFilter() {
            switch(currentFilter) {
                case 'sepia':
                    return new filters.Sepia();
                case 'vintage':
                    return new filters.Vintage();
                case 'invert':
                    return new filters.Invert();
                case 'polaroid':
                    return new filters.Polaroid();
                case 'grayscale':
                    return new filters.Grayscale();
                default:
                    return null;
            }
        }
        const filter = getSelectedFilter();
        const img = canvas.getActiveObject();

        img.filters=filter ? [filter] : [];
        img.applyFilters();
        canvas.renderAll();
    }, [currentFilter, canvas]);

    const [drawingMode, setDrawingMode] = useState(false);

    function toggleDrawingMode() {
        canvas.isDrawingMode = !canvas.isDrawingMode;
        setDrawingMode(canvas.isDrawingMode);
    }

    function addText() {
        const text = new IText('Edit this text');
        canvas.add(text);
        canvas.centerObject(text);
        canvas.setActiveObject(text);
    }

    function downloadImage() {
        const link = document.createElement('a');
        link.download = 'photo.png';
        link.href = canvas.toDataURL();
        link.click();
    }

    return (
        <div className="toolbox">
            <button title="Add image">
                <FontAwesomeIcon icon="image"/>
                <input
                    type="file"
                    accept=".png, .jpg, .jpeg"
                    onChange={fileHandler}/>
            </button>

            <button title="Add text" onClick={addText}>
                <FontAwesomeIcon icon="font"/>
            </button>

            <button title="Drawing mode" onClick={toggleDrawingMode} className={drawingMode ? 'active' : ''}>
                <FontAwesomeIcon icon="pencil"/>
            </button>

            <button title="Filters"
                    onClick={() => setCurrentFilter(currentFilter ? null : 'grayscale')}
                    className={currentFilter ? 'active' : ''}>
                <FontAwesomeIcon icon="filter"/>
            </button>
            {currentFilter &&
                <select onChange={(e) => setCurrentFilter(e.target.value)} value={currentFilter}>
                    <option value="invert">Invert</option>
                    <option value="grayscale">Grayscale</option>
                </select>
            }

            <button title="Download as image" onClick={downloadImage}>
                <FontAwesomeIcon icon="download"/>
            </button>

        </div>
    );
};

export default Toolbox;