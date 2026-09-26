import './App.css'
import { TwitterFollowCard } from './TwitterFollowCard.jsx'
//react renderiza elementos
//el componente es la factoria de elementos

//las props deben ser inmutables

const users = [
  {
    userName: 'janeD',
    name: 'Jane Doe',
    isFollowing: true
  },
  {
    userName: 'JohnD',
    name: 'John Doe',
    isFollowing: false
  },
  {
    userName: 'SuperMax',
    name: 'Maxine Caufield',
    isFollowing: true
  },
  {
    userName: 'AChen',
    name: 'Alex Chen',
    isFollowing: false
  }
]

//en js las funciones son ciudadanos de primera clase
export function App() {
  return (
    //utilizamos className como selector de clase porque en js class es una palabra reservada
    <section className='App'>
      {users.map(({ userName, name, isFollowing }) => (
        <TwitterFollowCard
          key={userName} //la key es un id único de ese elemento
          userName={userName}
          initialIsFollowing={isFollowing}
        >
          {name} //children
        </TwitterFollowCard>
      ))}
    </section>
  )
}