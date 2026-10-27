
import { useEffect, useState } from 'react'
import { getCategories } from './services/categoryService'

function App() {
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    async function loadCategories() {
      try {
        const data = await getCategories()
        setCategories(data)
      } catch (err) {
        setError(err.message || 'Something went wrong')
      } finally {
        setLoading(false)
      }
    }

    loadCategories()
  }, [])

  if (loading) {
    return <p>Loading categories...</p>
  }

  if (error) {
    return <p>Error: {error}</p>
  }

  return (
    <main>
      <h1>SmartShop</h1>
      <h2>Category Management</h2>

      {categories.length === 0 ? (
        <p>No categories found.</p>
      ) : (
        <ul>
          {categories.map((category) => (
            <li key={category.id}>
              <strong>{category.name}</strong>
              <p>{category.description || 'No description'}</p>
            </li>
          ))}
        </ul>
      )}
    </main>
  )
}

export default App
