
import { useState } from 'react'
import './App.css'

function App() {
  const [categoryName, setCategoryName] = useState('')
  const [categories, setCategories] = useState([
    { id: 1, name: 'Groceries' },
    { id: 2, name: 'Beverages' },
  ])

  function addCategory() {
    const name = categoryName.trim()

    if (!name) {
      return
    }

    const newCategory = {
      id: crypto.randomUUID(),
      name: name,
    }

    setCategories((previousCategories) => [
      ...previousCategories,
      newCategory,
    ])

    setCategoryName('')
  }

  return (
    <main className="app-container">
      <h1>SmartShop</h1>
      <h2>Category Management</h2>

      <form
        className="category-form"
        onSubmit={(event) => {
          event.preventDefault()
          addCategory()
        }}
      >
        <input
          value={categoryName}
          onChange={(event) => setCategoryName(event.target.value)}
          placeholder="Enter category name"
          maxLength={100}
        />

        <button type="submit">Add Category</button>
      </form>

      <ul className="category-list">
        {categories.map((category) => (
          <li key={category.id}>{category.name}</li>
        ))}
      </ul>
    </main>
  )
}

export default App
