import React from 'react';
import '../Page.css';
import Cookies from 'js-cookie';

export default class SignIn extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            username: '',
            password: '',
        };
        this.getRequest = this.getRequest.bind(this);
        this.redirectToSignUp = this.redirectToSignUp.bind(this);
    }

    getRequest() {
        const username = this.state.username;
        const password = this.state.password;

        fetch('http://localhost:8000/users?username=' + username + '&password=' + password)
            .then(res => res.json())
            .then(res => {
                if (res['sign_up_success']) {
                    Cookies.set('username', username);
                    alert("Sign-in succeed!");
                    window.location = "http://localhost:3000/";
                } else {
                    alert("Invalid username or password");
                }
            });
    }

    redirectToSignUp() {
        window.location = "http://localhost:3000/signup";
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
                                <h5 className="card-title text-center">Sign In</h5>
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
                                            type="button" onClick={this.getRequest}>Sign in
                                    </button>

                                    <hr/>
                                    <button className="btn btn-lg btn-success btn-block text-uppercase"
                                            type="button" onClick={this.redirectToSignUp}>Don't have an account? Sign up
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