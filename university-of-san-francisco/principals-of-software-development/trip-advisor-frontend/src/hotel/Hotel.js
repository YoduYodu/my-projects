import React from "react";
import Cookies from 'js-cookie';
import qs from 'query-string';
import ReactDOM from "react-dom";
import ReviewItem from "./reviewItem/ReviewItem";


export default class Hotel extends React.Component {
    constructor(props) {
        super(props);
        var queries =  qs.parse(this.props.location.search);
        this.state = {
            username: Cookies.get('username'),
            hotelName: '[hotel name]',
            address: '[address]',
            hotelId: queries.hotelId,
            title: '',
            text: '',
            radius: 1,
        };
        this.addCustomReview = this.addCustomReview.bind(this);
        this.editCustomReview = this.editCustomReview.bind(this);
        this.deleteCustomReview = this.deleteCustomReview.bind(this);
        this.findAttractions =this.findAttractions.bind(this);
    }

    editCustomReview() {
        fetch('http://localhost:8000/customReview', {
            method: 'POST',
            body: JSON.stringify({'request': 'edit', 'username': this.state.username, 'hotelId': this.state.hotelId, 'title': this.state.title, 'text': this.state.text}),
            headers: {
                'Content-Type': 'text/plain',
            },
            mode: 'cors',
            credentials: 'include',
        }).then(
            ReactDOM.render((
                <div>
                    <input type="text" className="form-control search-slt"
                           placeholder={this.state.title}
                           onChange={e => {this.setState({title: e.target.value})}}/>
                    <input type="text" className="form-control search-slt"
                           placeholder={this.state.text}
                           onChange={e => {this.setState({text: e.target.value})}}/>
                    <button onClick={this.editCustomReview} type="button" className="btn btn-danger wrn-btn">Edit</button>
                    <button onClick={this.deleteCustomReview} type="button" className="btn btn-danger wrn-btn">Delete</button>
                </div>
            ), document.getElementById("customReview"))
        );
    }

    deleteCustomReview() {
        fetch('http://localhost:8000/customReview', {
            method: 'POST',
            body: JSON.stringify({'request': 'delete', 'username': this.state.username, 'hotelId': this.state.hotelId, 'title': this.state.title, 'text': this.state.text}),
            headers: {
                'Content-Type': 'text/plain',
            },
            mode: 'cors',
            credentials: 'include',
        }).then(
            ReactDOM.render((
                <div>
                    <p>You haven't reviewed yet</p>
                    <div className="row">
                        <input type="text" className="form-control search-slt"
                               placeholder="Enter Title"
                               onChange={e => {this.setState({title: e.target.value})}}/>
                    </div>
                    <br/>
                    <div className="row">
                        <input type="text" className="form-control search-slt"
                               placeholder="Enter Text"
                               onChange={e => {this.setState({text: e.target.value})}}/>
                    </div>
                    <br/>
                    <button onClick={this.addCustomReview} type="button" className="btn btn-danger wrn-btn">Add</button>
                </div>
            ), document.getElementById("customReview"))
        );
    }

    addCustomReview() {
        fetch('http://localhost:8000/customReview', {
            method: 'POST',
            body: JSON.stringify({'request': 'add', 'username': this.state.username, 'hotelId': this.state.hotelId, 'title': this.state.title, 'text': this.state.text}),
            headers: {
                'Content-Type': 'text/plain',
            },
            mode: 'cors',
            credentials: 'include',
        }).then(
            ReactDOM.render((
                <div>
                    <input type="text" className="form-control search-slt"
                           placeholder={this.state.title}
                           onChange={e => {this.setState({title: e.target.value})}}/>
                    <input type="text" className="form-control search-slt"
                           placeholder={this.state.text}
                           onChange={e => {this.setState({text: e.target.value})}}/>
                    <button onClick={this.editCustomReview} type="button" className="btn btn-danger wrn-btn">Edit</button>
                    <button onClick={this.deleteCustomReview} type="button" className="btn btn-danger wrn-btn">Delete</button>
                </div>
            ), document.getElementById("customReview"))
        );

    }

    componentDidMount() {
        fetch('http://localhost:8000/customReview?username=' + this.state.username + '&hotelId=' + this.state.hotelId)
            .then(res => res.json())
            .then(customReview => {
                if (customReview['failed']) {
                    ReactDOM.render((
                        <div>
                            <p>You haven't reviewed yet</p>
                            <div className="row">
                                <input type="text" className="form-control search-slt"
                                       placeholder="Enter Title"
                                       onChange={e => {this.setState({title: e.target.value})}}/>
                            </div>
                            <br/>
                            <div className="row">
                                <input type="text" className="form-control search-slt"
                                       placeholder="Enter Text"
                                       onChange={e => {this.setState({text: e.target.value})}}/>
                            </div>
                            <br/>
                            <button onClick={this.addCustomReview} type="button" className="btn btn-danger wrn-btn">Add</button>
                        </div>
                    ), document.getElementById("customReview"));
                } else {
                    this.setState({
                        title: customReview['title'],
                        text: customReview['text'],
                    });
                    ReactDOM.render((
                        <div>
                            <input type="text" className="form-control search-slt"
                                   placeholder={this.state.title}
                                   onChange={e => {this.setState({title: e.target.value})}} />

                            <input type="text" className="form-control search-slt"
                                   placeholder={this.state.text}
                                   onChange={e => {this.setState({text: e.target.value})}} />
                            <button onClick={this.editCustomReview} type="button" className="btn btn-danger wrn-btn">Edit</button>
                            <button onClick={this.deleteCustomReview} type="button" className="btn btn-danger wrn-btn">Delete</button>
                        </div>
                    ), document.getElementById("customReview"));
                }
            });
    }

    findAttractions() {
        fetch('http://localhost:8000/attractions?hotelId=' + this.state.hotelId + '&radius=' + this.state.radius)
            .then(res => res.json())
            .then(res => {
                let attractions = res['results'];
                ReactDOM.render((
                    <div className="container">
                        <div className="card border-0 shadow my-5">
                            <div className="card-body p-5 pre-scrollable">
                                <ul className="list-group">
                                    {attractions.map(attraction => {
                                        return (
                                            <div>
                                                <li className="list-group-item d-flex justify-content-between align-items-center">
                                                    {attraction['name']}
                                                </li>
                                                <li className="list-group-item d-flex justify-content-between align-items-center">
                                                    {attraction['formatted_address']}
                                                </li>
                                                <br/>
                                            </div>
                                        )
                                    })}
                                </ul>
                            </div>
                        </div>
                    </div>
                ), document.getElementById('attractions'));
            });
    }

    render() {
        if (Cookies.get('username') === undefined) {
            window.location = "http://localhost:3000/signin";
        } else if (this.state.hotelId === undefined) {
            window.location = "http://localhost:3000/hotel";
        }

        fetch('http://localhost:8000/hotels?hotelId=' + this.state.hotelId)
            .then(res => res.json())
            .then(hotel => {
                this.setState({
                    hotelName: hotel['hotelName'],
                    address: hotel['address'] + ' ' + hotel['city'] + ' ' + hotel['state'],
                })
            });


        fetch('http://localhost:8000/reviews?hotelId=' + this.state.hotelId)
            .then(res => res.json())
            .then(res => {
                let reviews = res['result'];
                if (!reviews) {
                    ReactDOM.render((
                        <div className="container">
                            <div className="card border-0 shadow my-5">
                                <div className="card-body p-5 pre-scrollable">
                                    <ul className="list-group">
                                        <p>No reviews for this hotel</p>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    ), document.getElementById('reviews'));
                } else {
                    ReactDOM.render((
                        <div className="container">
                            <div className="card border-0 shadow my-5">
                                <div className="card-body p-5 pre-scrollable">
                                    <ul className="list-group">
                                        {reviews.map(review => {
                                            return (
                                                <div>
                                                    <ReviewItem reviewId={review['hotelId']}
                                                                rating={review['rating']}
                                                                title={review['title']}
                                                                text={review['text']}
                                                                userNickName={review['userNickName']}
                                                                submissionTime={review['submissionTime']}
                                                                isRecommend={review['isRecommend']}
                                                                likes={review['likes']} />
                                                    <br/>
                                                </div>
                                            )
                                        })}
                                    </ul>
                                </div>
                            </div>
                        </div>
                    ), document.getElementById('reviews'));
                }
            });

        return (
            <div>
                <nav className="navbar navbar-expand-lg navbar-light bg-light static-top mb-5 shadow">
                    <div className="container">
                        <a className="navbar-brand">{this.state.hotelName}</a>
                        <a href="#" className="navbar-text">{this.state.address}</a>
                        <div className="collapse navbar-collapse" id="navbarResponsive">
                            <ul className="navbar-nav ml-auto">
                                <li className="nav-item">
                                    <a href="http://localhost:3000/" className="nav-link">Search Page</a>
                                </li>
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
                            <p className="lead">Hotel Location:</p>
                            <p className="lead">[Google Map]</p>
                        </div>
                    </div>
                </div>

                <div id="reviews" />

                <div className="container">
                    <div className="card border-0 shadow my-5">
                        <div className="card-body p-5">
                            <p className="lead">Your Review:</p>
                            <div id="customReview" />
                        </div>
                    </div>
                </div>

                <div className="container">
                    <div className="container">
                        <form>
                            <div className="row">
                                <div className="col-lg-12">
                                    <div className="row">
                                        <div className="col-lg-3 col-md-3 col-sm-12 p-0">
                                            <input type="text" className="form-control search-slt"
                                                   placeholder="Enter Radius"
                                                   onChange={e => this.setState(
                                                       {radius: e.target.value}
                                                   )}
                                            />
                                        </div>

                                        <div className="col-lg-3 col-md-3 col-sm-12 p-0">
                                            <button onClick={this.findAttractions}
                                                    type="button" className="btn btn-danger wrn-btn">Display Attractions</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>

                <div id="attractions" />

            </div>

        )
    }
}