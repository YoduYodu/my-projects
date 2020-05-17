import React from "react";
import {Dialog, DialogContent} from "@material-ui/core";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContentText from "@material-ui/core/DialogContentText";
import Button from "@material-ui/core/Button";

export default class DialogFeedback extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      userId: '',
      predictionSentiment: false,
    }
  }

  handleSubmitCorrect() {
    fetch('http://localhost:8000/predictions/', {
      method: 'POST',
      body: JSON.stringify({
        'feedback': true,
        'user_id': this.state.userId
      }),
      headers: {
        'Content-Type': 'text/plain',
      },
    });

    this.props.onClose();
    window.location.reload();
  }

  handleSubmitInCorrect() {
    fetch('http://localhost:8000/predictions/', {
      method: 'POST',
      body: JSON.stringify({
        'feedback': false,
        'user_id': this.state.userId
      }),
      headers: {
        'Content-Type': 'text/plain',
      },
    });

    this.props.onClose();
    window.location.reload();
  }



  render() {
    return (
      <Dialog open={this.props.open} onClose={this.props.onClose} aria-labelledby="Feedback">
        <DialogTitle id="form-dialog-title" onClose={this.props.onClose}>
          Feedback
        </DialogTitle>

        <DialogContent>
          <DialogContentText>
            We think the sentiment is: {this.state.predictionSentiment === true ? 'Positive' : 'Negative'}
          </DialogContentText>
          <DialogContentText>
            Do you think the result is accurate? Please choose your answer down here:
          </DialogContentText>

          <Button onClick={this.handleSubmitCorrect.bind(this)}>
            Accurate
          </Button>

          <Button onClick={this.handleSubmitInCorrect.bind(this)}>
            Inaccurate
          </Button>
        </DialogContent>
      </Dialog>

    )
  }

}