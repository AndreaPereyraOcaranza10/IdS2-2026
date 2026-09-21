import React, {useState, useEffect} from 'react';
import {v4 as uuid} from 'uuid';
import './App.css';
import Header from './Header';
import AddContact from './AddContacts';
import ContactList from './ContactList';



function App() {
  const LOCAL_STORAGE_KEY = "contacts";
  const [contacts, setContacts] = useState([]);
  const [isLoaded, setIsLoaded] = useState(false);

  const addContactHandler = (contact) => {
    console.log(contact);
    setContacts([...contacts, {id: uuid(), ...contact}]);
  }

  const removeContactHandler = (id) => {
    const newContactList = contacts.filter((contact) => {
      return contact.id !== id;
    });

    setContacts(newContactList);
  }

  useEffect(() => {
      const retriveContacts = JSON.parse(localStorage.getItem(LOCAL_STORAGE_KEY));
      if(retriveContacts) setContacts(retriveContacts);
      setIsLoaded(true);
  }, []);

  useEffect(() => {
    if(isLoaded){
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(contacts));
    }
  }, [contacts]);

  return (
    <div className="ui container">
      <Header />
      <AddContact AddContactHandler = {addContactHandler} />
      <ContactList contacts={contacts} getContactId={removeContactHandler} />
       
              
    </div>
  );
}

export default App;
