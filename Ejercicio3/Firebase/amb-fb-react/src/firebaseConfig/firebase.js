
import { initializeApp } from "firebase/app";
import {getFirestore} from 'firebase/firestore'

const firebaseConfig = {
  apiKey: "AIzaSyDXHaaJjIyNlX3kTgIYCFmh7EWDkeoLpgk",
  authDomain: "amb-fire-react.firebaseapp.com",
  projectId: "amb-fire-react",
  storageBucket: "amb-fire-react.firebasestorage.app",
  messagingSenderId: "771101759044",
  appId: "1:771101759044:web:8c9a203d302be02f410e11"
};


const app = initializeApp(firebaseConfig);

export const db = getFirestore(app)