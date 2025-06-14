// Cargar todos los productos al iniciar la página
document.addEventListener('DOMContentLoaded', () => {
    loadProducts();
});

// --- LÓGICA PARA CREAR PRODUCTOS ---
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

        if (!response.ok) throw new Error('Error al crear el producto.');

        const createdProduct = await response.json();
        alert(`Producto '${createdProduct.name}' creado en MAESTRO.`);
        document.getElementById('create-form').reset();
        loadProducts(); // Recargar la lista de productos
    } catch (error) {
        console.error("Error en creación:", error);
        alert("Ocurrió un error al crear el producto.");
    }
});


// --- LÓGICA PARA LEER (LISTAR) PRODUCTOS ---
async function loadProducts() {
    try {
        const response = await fetch('/api/ventas/productos');
        if (!response.ok) throw new Error('Error al cargar los productos.');

        const products = await response.json();
        const tableBody = document.getElementById('products-table-body');
        tableBody.innerHTML = ''; // Limpiar la tabla antes de llenarla

        if(products.length === 0){
             tableBody.innerHTML = '<tr><td colspan="4" class="text-center">No hay productos para mostrar.</td></tr>';
             return;
        }

        products.forEach(product => {
            const row = `
                <tr>
                    <td>${product.name}</td>
                    <td>$${product.price.toFixed(2)}</td>
                    <td>${product.stock}</td>
                    <td>
                        <button class="btn btn-sm btn-info" onclick='showEditModal(${JSON.stringify(product)})'>Editar</button>
                        <button class="btn btn-sm btn-danger" onclick="deleteProduct('${product.id}', '${product.name}')">Eliminar</button>
                    </td>
                </tr>
            `;
            tableBody.innerHTML += row;
        });
    } catch (error) {
        console.error("Error en carga:", error);
        alert("Ocurrió un error al cargar la lista de productos.");
    }
}


// --- LÓGICA PARA ACTUALIZAR PRODUCTOS ---
function showEditModal(product) {
    document.getElementById('edit-id').value = product.id;
    document.getElementById('edit-name').value = product.name;
    document.getElementById('edit-price').value = product.price;
    document.getElementById('edit-stock').value = product.stock;
    $('#editProductModal').modal('show');
}

document.getElementById('edit-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('edit-id').value;
    const product = {
        name: document.getElementById('edit-name').value,
        price: parseFloat(document.getElementById('edit-price').value),
        stock: parseInt(document.getElementById('edit-stock').value)
    };

    try {
        const response = await fetch(`/api/ventas/productos/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(product)
        });

        if (!response.ok) throw new Error('Error al actualizar el producto.');

        $('#editProductModal').modal('hide');
        alert(`Producto '${product.name}' actualizado en MAESTRO.`);
        loadProducts(); // Recargar la lista
    } catch (error) {
        console.error("Error en actualización:", error);
        alert("Ocurrió un error al actualizar el producto.");
    }
});


// --- LÓGICA PARA ELIMINAR PRODUCTOS ---
async function deleteProduct(id, name) {
    if (!confirm(`¿Estás seguro de que quieres eliminar el producto '${name}'?`)) return;

    try {
        const response = await fetch(`/api/ventas/productos/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Error al eliminar el producto.');
        
        alert(`Producto '${name}' eliminado del MAESTRO.`);
        loadProducts(); // Recargar la lista
    } catch (error) {
        console.error("Error en eliminación:", error);
        alert("Ocurrió un error al eliminar el producto.");
    }
}