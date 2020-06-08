import React from "react";
import ReactDOM from 'react-dom';
import Cookies from 'js-cookie';
import HotelItem from "./hotelItem/HotelItem";

export default class Search extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            username: Cookies.get('username'),
            city: '',
            keyword: '',
            a: 0,
        };
        this.search = this.search.bind(this);
    }

    search() {
        fetch('http://localhost:8000/hotels?city='
            + this.state.city + '&keyword=' + this.state.keyword)
            .then(res => res.json())
            .then(res => {
                let hotelList = res['result'];
                if (hotelList.length === 0) {
                    alert("Couldn't find anything");
                    ReactDOM.render(<div/>, document.getElementById('searchList'));
                    return;
                }

                ReactDOM.render((<div className="container">
                    <div className="card border-0 shadow my-5">
                        <div className="card-body p-5 pre-scrollable">
                            <ul className="list-group">
                                {hotelList.map(hotel => {
                                    return <HotelItem hotelId={hotel['hotelId']}
                                                      hotelName={hotel['hotelName']}
                                                      address={hotel['address']}
                                                      city={hotel['city']}
                                                      state={hotel['state']}
                                                      averageRating={hotel['averageRating']} />
                                })}
                            </ul>
                        </div>
                    </div>
                </div>), document.getElementById('searchList'));
            });
    }

    render() {
        if (Cookies.get('username') === undefined) {
            window.location = "http://localhost:3000/signin";
        }

        return (
            <div>
                <nav className="navbar navbar-expand-lg navbar-light bg-light static-top mb-5 shadow">
                    <div className="container">
                        <a className="navbar-brand">Hotel Search</a>
                        <button className="navbar-toggler" type="button" data-toggle="collapse"
                                data-target="#navbarResponsive" aria-controls="navbarResponsive" aria-expanded="false"
                                aria-label="Toggle navigation">
                            <span className="navbar-toggler-icon"></span>
                        </button>
                        <div className="collapse navbar-collapse" id="navbarResponsive">
                            <ul className="navbar-nav ml-auto">
                                <li className="nav-item">
                                    <a className="nav-link">Saved</a>
                                </li>
                                <li className="nav-item active">
                                    <a onClick={() => {
                                        Cookies.remove('username');
                                        window.location = "http://localhost:3000/signin";
                                    }} className="nav-link">Log out
                                        <span className="sr-only">(current)</span>
                                    </a>
                                </li>


                            </ul>
                        </div>
                    </div>
                </nav>

                <div className="container">
                    <div className="card border-0 shadow my-5">
                        <div className="card-body p-5">
                            <h1 className="font-weight-light">Hello, {this.state.username}</h1>
                            <p className="lead m-1">Your last login is:</p>
                        </div>
                    </div>
                </div>

                <div className="container">
                    <div className="card border-0 shadow my-5">
                        <div className="card-body p-5">
                            <p className="lead">Browse all hotels:</p>
                            <p className="lead">[Google Map]</p>
                        </div>
                    </div>
                </div>

                <div className="container">
                    <div className="container">
                        <form>
                            <div className="row">
                                <div className="col-lg-12">
                                    <div className="row">
                                        <div className="col-lg-4 col-md-6 col-sm-12 p-0">
                                            <input type="text" className="form-control search-slt"
                                                   placeholder="Enter City"
                                                   onChange={e => {this.setState({city: e.target.value})}}/>
                                        </div>

                                        <div className="col-lg-5 col-md-6 col-sm-12 p-0">
                                            <input type="text" className="form-control search-slt"
                                                   placeholder="Enter Keyword"
                                                   onChange={e => {this.setState({keyword: e.target.value})}}/>
                                        </div>

                                        <div className="col-lg-3 col-md-3 col-sm-12 p-0">
                                            <button onClick={this.search}
                                                    type="button" className="btn btn-danger wrn-btn">Search</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>

                <div id="searchList" />

            </div>

        )
    }
}