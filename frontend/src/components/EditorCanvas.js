/**
 * Editor Canvas Component
 * 
 * Renders the main canvas for image editing using Fabric.js.
 * 
 * Features:
 * - Displays the Fabric.js canvas
 * - Tracks selected objects and their active filters
 * - Delete key support for removing objects
 * - Syncs current filter state with parent component
 * 
 * Props:
 * @param {Object} canvas - Fabric.js canvas instance
 * @param {Function} setCurrentFilter - Updates parent with active filter type
 * @param {Ref} ref - Forwarded ref to the canvas DOM element
 * 
 * Main Author/Source:
 * https://blog.logrocket.com/build-image-editor-fabric-js-v6/
 */

import { forwardRef, useEffect } from 'react';

const EditorCanvas = forwardRef(({ canvas, setCurrentFilter }, ref) => {

    /**
    * Checks canvas selection to track active filters
    * When an object with a filter is selected, updates parent component
    * so the Toolbox can show the correct active filter
    */
    useEffect(() => {
        if (!canvas) return;
        /**
         * Handles selection changes on the canvas
         * Extracts filter from selected object if it exists
         */

        function handleSelection(e) {
            // Get the selected object (only if exactly one is selected)
            const obj = e.selected?.length === 1 ? e.selected[0] : null;
            // Check if object has any filters applied
            const filter = obj?.filters?.at(0);
            // Update parent with current filter type (or null if none)
            setCurrentFilter(filter ? filter.type.toLowerCase() : null);
        }

        // Listen to events
        canvas.on({
            'selection:created': handleSelection,   // When object is selected
            'selection:updated': handleSelection,   // When selection changes
            'selection:cleared': handleSelection    // When selection is removed
        });

        // Remove event listeners on unmount
        return () => {
            canvas.off({
                'selection:created': handleSelection,
                'selection:updated': handleSelection,
                'selection:cleared': handleSelection
            });
        }
    }, [canvas, setCurrentFilter]);

    // Adds keyboard shortcuts for canvas operations
    useEffect(() => {
        function handleKeyDown(e) {
            if (e.key === 'Delete') {
                // Delete active objects from the canvas
                const activeObjects = canvas.getActiveObjects();
                if (activeObjects.length > 0) {
                    activeObjects.forEach((obj) => {
                        canvas.remove(obj);  // Remove object from canvas
                    });
                    canvas.discardActiveObject();  // Clear the selection
                    canvas.renderAll();  // Re-render the canvas
                }
            }
        }

        document.addEventListener('keydown', handleKeyDown);

        // Cleanup
        return () => {
            document.removeEventListener('keydown', handleKeyDown);
        };

    }, [canvas]);


    return (
        <div className="canvasbox">
            <canvas ref={ref} width="1000" height="500"></canvas>
        </div>
    );
});

export default EditorCanvas;