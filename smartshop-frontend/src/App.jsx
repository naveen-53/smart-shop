
import { useEffect, useState } from 'react'
import {
  getCategories,
  createCategory,
  updateCategory,
  updateCategoryStatus,
} from './services/categoryService'
import './App.css'

function App() {
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [editingId, setEditingId] = useState(null)

  const [form, setForm] = useState({
    name: '',
    description: '',
  })

  // Load categories when the component first mounts
  useEffect(() => {
    loadCategories()
  }, [])

  // GET: Fetch categories from the backend
  async function loadCategories() {
    try {
      setError('')
      const data = await getCategories()
      setCategories(data)
    } catch (err) {
      setError(err.message || 'Failed to load categories')
    } finally {
      setLoading(false)
    }
  }

  // Update form state when the user types
  function handleChange(event) {
    const { name, value } = event.target

    setForm((previousForm) => ({
      ...previousForm,
      [name]: value,
    }))

    setError('')
    setSuccess('')
  }

  // Load an existing category into the form
  function handleEdit(category) {
    setEditingId(category.id)

    setForm({
      name: category.name,
      description: category.description || '',
    })

    setError('')
    setSuccess('')
  }

  // Reset the form and exit edit mode
  function resetForm() {
    setForm({
      name: '',
      description: '',
    })

    setEditingId(null)
    setError('')
    setSuccess('')
  }

  // POST a new category or PUT an existing category
  async function handleSubmit(event) {
    event.preventDefault()

    const name = form.name.trim()
    const description = form.description.trim()

    if (!name) {
      setError('Category name is required.')
      return
    }

    if (name.length > 100) {
      setError('Category name cannot exceed 100 characters.')
      return
    }

    if (description.length > 500) {
      setError('Description cannot exceed 500 characters.')
      return
    }

    setSaving(true)
    setError('')
    setSuccess('')

    // Capture the mode before resetting the form
    const isEditing = editingId !== null

    try {
      const categoryData = { name, description }

      if (isEditing) {
        await updateCategory(editingId, categoryData)
      } else {
        await createCategory(categoryData)
      }

      resetForm()

      setSuccess(
        isEditing
          ? 'Category updated successfully.'
          : 'Category created successfully.'
      )

      await loadCategories()
    } catch (err) {
      setError(err.message || 'Failed to save category')
    } finally {
      setSaving(false)
    }
  }

  // PATCH: Activate or deactivate a category
  async function handleToggleStatus(category) {
    const newActiveStatus = !category.active

    setSaving(true)
    setError('')
    setSuccess('')

    try {
      await updateCategoryStatus(category.id, newActiveStatus)

      setSuccess(
        `Category ${newActiveStatus ? 'activated' : 'deactivated'} successfully.`
      )

      await loadCategories()
    } catch (err) {
      setError(err.message || 'Failed to update category status')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <main className="app-container">
        <p className="status-message">Loading categories...</p>
      </main>
    )
  }

  return (
    <main className="app-container">
      <h1>SmartShop</h1>
      <h2>Category Management</h2>

      {/* Category creation and editing form */}
      <form className="category-form" onSubmit={handleSubmit}>
        <div className="form-field">
          <label htmlFor="categoryName">Category name *</label>
          <input
            id="categoryName"
            name="name"
            value={form.name}
            onChange={handleChange}
            placeholder="Enter category name"
            maxLength={100}
            required
            disabled={saving}
          />
        </div>

        <div className="form-field">
          <label htmlFor="categoryDescription">Description</label>
          <textarea
            id="categoryDescription"
            name="description"
            value={form.description}
            onChange={handleChange}
            placeholder="Enter description"
            maxLength={500}
            rows={3}
            disabled={saving}
          />
        </div>

        {error && (
          <p className="error-message" role="alert">
            {error}
          </p>
        )}

        {success && (
          <p className="success-message" role="status">
            {success}
          </p>
        )}

        <div className="form-actions">
          <button type="submit" disabled={saving}>
            {saving
              ? 'Saving...'
              : editingId !== null
                ? 'Save Changes'
                : 'Add Category'}
          </button>

          {editingId !== null && (
            <button
              type="button"
              className="secondary-button"
              onClick={resetForm}
              disabled={saving}
            >
              Cancel Edit
            </button>
          )}
        </div>
      </form>

      {/* Category list */}
      <h2>Categories ({categories.length})</h2>

      {categories.length === 0 ? (
        <p>No categories found.</p>
      ) : (
        <ul className="category-list">
          {categories.map((category) => (
            <li key={category.id}>
              <div className="category-details">
                <strong>{category.name}</strong>

                <p>
                  {category.description || 'No description'}
                </p>

                <span
                  className={
                    category.active
                      ? 'status-badge active'
                      : 'status-badge inactive'
                  }
                >
                  {category.active ? 'Active' : 'Inactive'}
                </span>
              </div>

              <div className="category-actions">
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => handleEdit(category)}
                  disabled={saving}
                >
                  Edit
                </button>

                <button
                  type="button"
                  className={
                    category.active
                      ? 'danger-button'
                      : 'activate-button'
                  }
                  onClick={() => handleToggleStatus(category)}
                  disabled={saving}
                >
                  {category.active ? 'Deactivate' : 'Activate'}
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </main>
  )
}

export default App
