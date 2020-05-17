import React from 'react';
import '../Page.css';
import Cookies from "js-cookie";

export default class SignUp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            username: '',
            password: '',
        };
        this.postRequest = this.postRequest.bind(this);
        this.redirectToSignIn = this.redirectToSignIn.bind(this);
        this.checkPassword = this.checkPassword.bind(this);
    }

    postRequest() {
        const username = this.state.username;
        const password = this.state.password;

        if (!this.checkPassword(password)) {
            alert('Password length should be between 5 to 10 characters, contains at least one number, one letter and one special character');
            return;
        }

        fetch('http://localhost:8000/users', {
            method: 'POST',
            body: JSON.stringify({'username': username, 'password': password}),
            headers: {
                'Content-Type': 'text/plain',
            },
            mode: 'cors',
            credentials: 'include',
        }).then(response => response.json())
        .then(jsonObj => {
            if (jsonObj['sign_up_success']) {
                alert("Sign-up succeed!");
                Cookies.set('username', username);
                window.location = "http://localhost:3000/";
            } else {
                alert('Username is taken, please try another one');
            }
        });
    }

    checkPassword(password) {
        if (password.length >= 5 && password.length <= 10) {
            let letterRegex = new RegExp('[a-zA-Z]');
            let digitRegex = new RegExp('\\d');
            let specialRegex = new RegExp('\\W');
            if (password.search(letterRegex) !== -1
                && password.search(digitRegex) !== -1
                && password.search(specialRegex) !== -1) {
                return true;
            }
        }
        return false;
    }

    redirectToSignIn() {
        window.location = "http://localhost:3000/signin";
    }

    render() {
        if (Cookies.get('username') !== undefined) {
            window.location = "http://localhost:3000/";
        }

        return (
            <div className="container">
                <div className="row">
                    <div className="col-sm-9 col-md-7 col-lg-5 mx-auto">
                        <div className="card card-signin my-5">
                            <div className="card-body">
                                <h5 className="card-title text-center">Sign Up</h5>
                                <form className="form-signin">

                                    <div className="form-label-group">
                                        <input type="text" id="inputEmail" className="form-control"
                                               placeholder="Email address" required autoFocus
                                               onChange={e => {
                                                   this.setState({username: e.target.value});
                                               }}
                                        />
                                        <label htmlFor="inputEmail">Username</label>
                                    </div>

                                    <div className="form-label-group">
                                        <input type="password" id="inputPassword" className="form-control"
                                               placeholder="Password" required
                                               onChange={e => {
                                                   this.setState({password: e.target.value});
                                               }}
                                        />
                                        <label htmlFor="inputPassword">Password</label>
                                    </div>

                                    <hr/>
                                    <button className="btn btn-lg btn-primary btn-block text-uppercase"
                                            type="button"
                                            onClick={this.postRequest}>
                                        Sign Up
                                    </button>

                                    <hr/>
                                    <button className="btn btn-lg btn-success btn-block text-uppercase"
                                            type="button"
                                            onClick={this.redirectToSignIn}>
                                        Already have an account? Sign In
                                    </button>

                                </form>
                            </div>
                        </div>
                    </div>
                </div>

            </div>

        );
    }
}