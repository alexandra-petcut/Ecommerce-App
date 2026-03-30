import { useState, useRef } from 'react'
import { getProductImageUrl } from '../services/api'

const emptyForm = { name: '', description: '', price: '', stock: '' }

function ProductList({ products, loading, onAddToCart, isLoggedIn, isAdmin, onCreateProduct, onUpdateProduct }) {
  const [showForm, setShowForm] = useState(false) // for "Add Product" form
  const [editingId, setEditingId] = useState(null) // which product is being edited
  const [form, setForm] = useState(emptyForm)
  const [imageFile, setImageFile] = useState(null) // selected file for upload
  const [imagePreview, setImagePreview] = useState(null) // preview URL for selected file
  const [saving, setSaving] = useState(false)
  const fileInputRef = useRef(null)

  // Format price with comma separators (e.g. 55000 -> "55,000.00")
  function formatPrice(price) {
    return price.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })
  }

  // Return label and CSS class based on stock quantity
  function getStockBadge(stock) {
    if (stock === 0) return { label: 'Out of stock', className: 'out-of-stock' }
    if (stock <= 5) return { label: `Only ${stock} left!`, className: 'low-stock' }
    return { label: 'In stock', className: 'in-stock' }
  }

  function handleChange(e) {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  function handleFileChange(e) {
    const file = e.target.files[0]
    if (file) {
      setImageFile(file)
      setImagePreview(URL.createObjectURL(file)) // create a temporary browser URL for preview
    }
  }

  function clearFileInput() {
    setImageFile(null)
    setImagePreview(null)
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  function openCreateForm() {
    setEditingId(null)
    setForm(emptyForm)
    clearFileInput()
    setShowForm(true)
  }

  function openEditForm(product) {
    setShowForm(false)
    setEditingId(product.id)
    setForm({
      name: product.name,
      description: product.description || '',
      price: String(product.price),
      stock: String(product.stock),
    })
    clearFileInput()
  }

  function cancelForm() {
    setShowForm(false)
    setEditingId(null)
    setForm(emptyForm)
    clearFileInput()
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)

    const payload = {
      name: form.name,
      description: form.description,
      price: parseFloat(form.price),
      stock: parseInt(form.stock, 10),
    }

    if (editingId) {
      await onUpdateProduct(editingId, payload, imageFile)
    } else {
      await onCreateProduct(payload, imageFile)
    }

    setSaving(false)
    cancelForm()
  }

  // Reusable form for both create and edit
  function renderForm() {
    return (
      <form className="product-form" onSubmit={handleSubmit}>
        <div className="product-form-grid">
          <div className="form-group">
            <label className="form-label" htmlFor="pf-name">Product name</label>
            <input className="form-input" id="pf-name" name="name" value={form.name} onChange={handleChange} placeholder="e.g. Wireless Headphones" required />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="pf-price">Price</label>
            <input className="form-input" id="pf-price" name="price" type="number" step="0.01" min="0.01" value={form.price} onChange={handleChange} placeholder="29.99" required />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="pf-stock">Stock</label>
            <input className="form-input" id="pf-stock" name="stock" type="number" min="0" value={form.stock} onChange={handleChange} placeholder="100" required />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="pf-image">Product image</label>
            <input
              className="form-input file-input"
              id="pf-image"
              type="file"
              accept="image/*"
              onChange={handleFileChange}
              ref={fileInputRef}
            />
          </div>
        </div>
        {imagePreview && (
          <div className="image-preview">
            <img src={imagePreview} alt="Preview" />
            <button type="button" className="image-preview-remove" onClick={clearFileInput}>Remove</button>
          </div>
        )}
        <div className="form-group">
          <label className="form-label" htmlFor="pf-desc">Description</label>
          <textarea className="form-input product-form-textarea" id="pf-desc" name="description" value={form.description} onChange={handleChange} placeholder="Describe the product..." rows={3} />
        </div>
        <div className="product-form-actions">
          <button type="submit" className="btn-primary" disabled={saving}>
            {saving ? 'Saving...' : editingId ? 'Save changes' : 'Add product'}
          </button>
          <button type="button" className="btn-outline" onClick={cancelForm}>Cancel</button>
        </div>
      </form>
    )
  }

  // Loading state
  if (loading) {
    return (
      <div>
        <div className="page-header">
          <h1>Products</h1>
          <p>Browse our collection</p>
        </div>
        <div className="loading-spinner">
          <div className="spinner"></div>
          <span>Loading products...</span>
        </div>
      </div>
    )
  }

  // Empty state
  if (products.length === 0 && !isAdmin) {
    return (
      <div>
        <div className="page-header">
          <h1>Products</h1>
        </div>
        <div className="empty-state">
          <h3>No products available</h3>
          <p>Check back later for new arrivals.</p>
        </div>
      </div>
    )
  }

  return (
    <div>
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <h1>Products</h1>
            <p>Browse our collection of {products.length} items</p>
          </div>
          {isAdmin && !showForm && editingId === null && (
            <button className="btn-primary" onClick={openCreateForm}>+ Add product</button>
          )}
        </div>
      </div>

      {/* Create product form */}
      {isAdmin && showForm && (
        <div className="product-form-card">
          <h3>New product</h3>
          {renderForm()}
        </div>
      )}

      <div className="products-grid">
        {products.map((product) => {
          const stock = getStockBadge(product.stock)
          const isEditing = editingId === product.id

          return (
            <article key={product.id} className="product-card">
              {isEditing ? (
                /* Inline edit form */
                <>
                  <h3 className="product-card-edit-title">Edit product</h3>
                  {renderForm()}
                </>
              ) : (
                /* Normal product display */
                <>
                  <div className="product-image">
                    <img
                      src={getProductImageUrl(product.id)}
                      alt={product.name}
                      onError={(e) => {
                        // If no image stored, hide the broken img and show fallback text
                        e.target.style.display = 'none'
                        e.target.parentElement.classList.add('no-image')
                      }}
                    />
                  </div>

                  <h3>{product.name}</h3>
                  <p className="product-description">{product.description}</p>

                  <div className="product-meta">
                    <span className="product-price">
                      ${formatPrice(product.price)}
                    </span>
                    <span className={`stock-badge ${stock.className}`}>
                      {stock.label}
                    </span>
                  </div>

                  {isAdmin ? (
                    <div className="product-card-admin-actions">
                      <button className="btn-outline" onClick={() => openEditForm(product)}>
                        Edit
                      </button>
                      <button
                        className="btn-primary"
                        onClick={() => onAddToCart(product.id)}
                        disabled={product.stock === 0}
                        title={product.stock === 0 ? 'This product is out of stock' : ''}
                      >
                        {product.stock === 0 ? 'Out of stock' : 'Add to cart'}
                      </button>
                    </div>
                  ) : (
                    <button
                      className="btn-primary"
                      onClick={() => onAddToCart(product.id)}
                      disabled={product.stock === 0 || !isLoggedIn}
                      title={
                        !isLoggedIn
                          ? 'Log in to add items to cart'
                          : product.stock === 0
                            ? 'This product is out of stock'
                            : ''
                      }
                    >
                      {product.stock === 0
                        ? 'Out of stock'
                        : !isLoggedIn
                          ? 'Log in to buy'
                          : 'Add to cart'}
                    </button>
                  )}
                </>
              )}
            </article>
          )
        })}
      </div>
    </div>
  )
}

export default ProductList
