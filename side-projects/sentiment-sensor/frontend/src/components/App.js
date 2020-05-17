import React from 'react';
import './App.css';
import Predictor from "./predictor/Predictor";
import Grid from "@material-ui/core/Grid";
import NavBar from "./bar/NavBar";
import DialogSignIn from "./popups/dialogSignIn/DialogSignIn";
import DialogSignUp from "./popups/dialogSignUp/DialogSignUp";
import DialogFeedback from "./popups/dialogFeedback/DialogFeedback";

class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSignIn: false,
      showSignUp: false,
      showFeedback: false,
    };
    this.dialogFeedbackElement = React.createRef();
  }

  handleClickSignIn() {
    this.setState({
      showSignIn: true,
      showSignUp: false,
    })
  }

  handleClickSignUp() {
    this.setState({
      showSignIn: false,
      showSignUp: true,
    })
  }

  handleOpenFeedback(predictionSentiment, userId) {
    this.dialogFeedbackElement.current.setState({
      userId: userId,
      predictionSentiment: predictionSentiment
    })
    this.setState({showFeedback: true})
  }


  handleClose() {
    this.setState({
      showSignIn: false,
      showSignUp: false,
      showFeedback: false,
    });
  }

  setPredictionId(id) {
    this.setState({
      predictionId: id,
    })
  }

  render() {
    return (
      <div>
        <Grid
          container
          direction="column"
          spacing={1}
        >

          <Grid item xs={12}>
            <Grid container>
              <Grid item xs={12}>
                <NavBar
                  onClickSignIn={this.handleClickSignIn.bind(this)}
                  onClickSignUp={this.handleClickSignUp.bind(this)}/>
              </Grid>
            </Grid>
          </Grid>

          <Grid item xs={12}>
            <Predictor setPredictionId={this.setPredictionId.bind(this)}
                       handleOpenFeedback={this.handleOpenFeedback.bind(this)}/>
          </Grid>

          <DialogSignIn open={this.state.showSignIn} onClose={this.handleClose.bind(this)} />
          <DialogSignUp open={this.state.showSignUp} onClose={this.handleClose.bind(this)} />
          <DialogFeedback
            ref={this.dialogFeedbackElement}
            open={this.state.showFeedback}
            onClose={this.handleClose.bind(this)} />

        </Grid>
      </div>
    );
  }
}

export default App;
