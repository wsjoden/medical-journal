/**
 * Image Edit Page
 * 
 * Features:
 * - Free drawing with pencil tool
 * - Image filters (brightness, contrast, etc.)
 * - Toolbox for editing controls
 * - Canvas for displaying and editing images
 */

import { useState, useRef, useEffect } from 'react';
import { Canvas, PencilBrush } from 'fabric';
import '../../styles/ImageEditPage.css';
import Toolbox from "../../components/Toolbox";
import EditorCanvas from "../../components/EditorCanvas";

/**
    * Initialize Fabric.js canvas on component mount
    * Sets up default drawing configuration (black pencil, 5px width)
    */
const ImageEditPage = () => {
    const canvasRef = useRef(null);
    const [canvas, setCanvas] = useState(null);
    const [currentFilter, setCurrentFilter] = useState(null);

    useEffect(() => {
        const canvas = new Canvas(canvasRef.current, {
            backgroundColor: 'white'
        });
        canvas.setDimensions({ width: 1000, height: 500 });

        const brush = new PencilBrush(canvas);
        brush.color = 'black';
        brush.width = 5;
        canvas.freeDrawingBrush = brush;
        setCanvas(canvas);

        // Cleanup
        return () => {
            canvas.dispose();
        };

    }, [canvasRef, setCanvas]);



    return (
        <div className="editor">
            <Toolbox
                canvas={canvas}
                currentFilter={currentFilter}
                setCurrentFilter={setCurrentFilter}
            />
            <EditorCanvas
                ref={canvasRef}
                canvas={canvas}
                setCurrentFilter={setCurrentFilter}
            />
        </div>
    )

};

export default ImageEditPage;
