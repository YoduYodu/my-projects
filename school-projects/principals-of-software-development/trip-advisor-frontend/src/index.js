import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import * as serviceWorker from './serviceWorker';
import { Route, BrowserRouter as Router } from 'react-router-dom';
import SignUp from "./signup/SignUp";
import SignIn from "./signin/SignIn";
import Search from "./search/Search";
import Hotel from "./hotel/Hotel";

const routing = (
    <Router>
        <Route exact path="/" component={Search} />
        <Route path="/hotel" component={Hotel} />
        <Route path="/signup" component={SignUp} />
        <Route path="/signin" component={SignIn} />
    </Router>
);

ReactDOM.render(routing, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
