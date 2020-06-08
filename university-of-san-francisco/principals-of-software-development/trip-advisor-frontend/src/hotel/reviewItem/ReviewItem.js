import React from "react";

export default class ReviewItem extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            reviewId: props['reviewId'],
            rating: props['rating'],
            title: props['title'],
            text: props['text'],
            userNickName: props['userNickName'],
            submissionTime: props['submissionTime'],
            isRecommend: props['isRecommend'],
            likes: props['likes'],
        }
    }

    render() {
        return (
            <div>
                <li className="list-group-item d-flex justify-content-between align-items-center">
                    {this.state.userNickName || 'Anonymous'} Rating: {this.state.rating} Date: {this.state.submissionTime}
                </li>
                <li className="list-group-item d-flex justify-content-between align-items-center">
                    Title: {this.state.title || 'None'}
                </li>

                <li className="list-group-item d-flex justify-content-between align-items-center">
                    {this.state.text || 'None'}
                </li>
                <li className="list-group-item d-flex justify-content-between align-items-center">
                    {this.state.likes || 0} people found this helpful!
                    <button onClick={this.search}
                            type="button" className="btn btn-danger wrn-btn">Like</button>
                </li>
            </div>
        )
    }
}