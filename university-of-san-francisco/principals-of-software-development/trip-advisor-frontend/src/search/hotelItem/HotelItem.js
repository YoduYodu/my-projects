import React from "react";

export default class HotelItem extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            hotelId: props['hotelId'],
            hotelName: props['hotelName'],
            averageRating: props['averageRating'],
        }
        this.save = this.save.bind(this);
        this.routeToHotel = this.routeToHotel.bind(this);
    }

    save() {
        // TODO: Send POST request to /savedHotels
    }

    routeToHotel() {
        window.location = "http://localhost:3000/hotel?hotelId=" + this.state.hotelId;
    }

    render() {
        return (
            <li className="list-group-item d-flex justify-content-between align-items-center">
                {/*<Router>*/}
                {/*    <Link to={'/hotel'}>*/}
                {/*        {this.state.hotelName}*/}
                {/*    </Link>*/}
                {/*</Router>*/}
                <a href={"http://localhost:3000/hotel?hotelId=" + this.state.hotelId} target="_blank" >{this.state.hotelName}</a>
                <span>Average Rating: {this.state.averageRating === 0? "No data" : this.state.averageRating}</span>

                <button className="btn btn-danger"
                        type="button" onClick={this.save}>Save
                </button>
            </li>
        );
    }
}