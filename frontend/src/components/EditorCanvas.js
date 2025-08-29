import {forwardRef, useEffect} from 'react';

const EditorCanvas = forwardRef(({canvas, setCurrentFilter}, ref) => {

    useEffect(() => {
        if(!canvas) return;

        function handleSelection(e) {
            const obj = e.selected?.length === 1 ? e.selected[0] : null;
            const filter = obj?.filters?.at(0);
            setCurrentFilter(filter ? filter.type.toLowerCase() : null);
        }

        canvas.on({
            'selection:created': handleSelection,
            'selection:updated': handleSelection,
            'selection:cleared': handleSelection
        });

        return () => {
            canvas.off({
                'selection:created': handleSelection,
                'selection:updated': handleSelection,
                'selection:cleared': handleSelection
            });
        }
    }, [canvas, setCurrentFilter]);

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