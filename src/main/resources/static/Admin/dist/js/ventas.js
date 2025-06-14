document.getElementById('create-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const product = {
        name: document.getElementById('name').value,
        price: parseFloat(document.getElementById('price').value),
        stock: parseInt(document.getElementById('stock').value)
    };

    try {
        const response = await fetch('/api/ventas/productos', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(product)
        });

        if (!response.ok) {
            throw new Error('La respuesta del servidor no fue exitosa.');
        }

        const createdProduct = await response.json();
        
        alert(`Producto creado en MAESTRO con ID: ${createdProduct.id}`);

        document.getElementById('search-id').value = createdProduct.id;
        
        document.getElementById('create-form').reset();

    } catch (error) {
        console.error("Error al crear el producto:", error);
        alert("Ocurrió un error al crear el producto. Revisa la consola.");
    }
});

async function searchProduct() {
    const id = document.getElementById('search-id').value;
    const infoDiv = document.getElementById('product-info');
    if (!id) return;

    infoDiv.innerHTML = 'Buscando en RÉPLICA...';
    try {
        const response = await fetch(`/api/ventas/productos/${id}`);
    
        if (response.ok) {
            const product = await response.json();
            if (product) {
                infoDiv.innerHTML = `
                <strong>Nombre:</strong> ${product.name}<br>
                <strong>ID:</strong> ${product.id}<br>
                <strong>Precio:</strong> $${product.price}<br>
                <strong>Stock:</strong> ${product.stock}
            `;
            } else {
                 infoDiv.innerHTML = `<span class="text-danger">Producto no encontrado.</span>`;
            }
        } else {
            infoDiv.innerHTML = `<span class="text-danger">Producto no encontrado (Error ${response.status}).</span>`;
        }
    } catch(error) {
        console.error("Error al buscar el producto:", error);
        alert("Ocurrió un error al buscar el producto. Revisa la consola.");
    }
}