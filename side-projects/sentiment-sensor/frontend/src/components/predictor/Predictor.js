import {Box, CssBaseline, makeStyles, Typography} from "@material-ui/core";
import Grid from "@material-ui/core/Grid";
import TextField from "@material-ui/core/TextField";
import React, {useEffect, useState} from "react";
import Button from "@material-ui/core/Button";
import Cookies from 'js-cookie';
import Link from "@material-ui/core/Link";
import DialogFeedback from "../popups/dialogFeedback/DialogFeedback";

const useStyles = makeStyles(theme => ({
  root: {
    flexGrow: 1,
  },
  myButton: {
    background: 'linear-gradient(45deg, #FE6B8B 30%, #FF8E53 90%)',
    border: 0,
    borderRadius: 3,
    boxShadow: '0 3px 5px 2px rgba(255, 105, 135, .3)',
    color: 'white',
    height: 48,
    padding: '0 30px',
  },
  paper: {
    padding: theme.spacing(2),
    margin: 'auto',
    maxWidth: 500,
  },
  textField: {
    width: 600,
  }
}));

export default function Predictor(props) {
  const classes = useStyles();

  const [text, setText] = useState('');
  const [user_id] = useState(Cookies.get('user_id') === '' ? 'Guest' : Cookies.get('user_id'));

  const [totalSubmission, setTotalSubmission] = useState('');
  const [totalPositive, setTotalPositive] = useState('');
  const [totalNegative, setTotalNegative] = useState('');
  const [totalAccuracy, setTotalAccuracy] = useState('');

  const [userSubmission, setUserSubmission] = useState('');
  const [userPositive, setUserPositive] = useState('');
  const [userNegative, setUserNegative] = useState('');
  const [userAccuracy, setUserAccuracy] = useState('');

  async function updateMetadata() {
    fetch('http://localhost:8000/metadata/', {method: 'GET'})
      .then(response => response.json())
      .then(jsonObj => {
        setTotalSubmission(jsonObj['total_submission']);
        setTotalPositive(jsonObj['total_positive']);
        setTotalNegative(jsonObj['total_negative']);

        let total_correct = jsonObj['total_correct'];
        let total_incorrect = jsonObj['total_incorrect'];
        let accuracy = (total_correct + total_incorrect === 0) ? 100 : (100 * total_correct / (total_correct + total_incorrect)).toFixed(2);
        setTotalAccuracy(accuracy);
      });
  }

  async function updateUserData() {
    fetch('http://localhost:8000/users/?user_id=' + user_id)
      .then(response => response.json())
      .then(jsonObj => {
        setUserSubmission(jsonObj['user_submission']);
        setUserPositive(jsonObj['user_positive']);
        setUserNegative(jsonObj['user_negative']);

        let user_correct = jsonObj['user_correct'];
        let user_incorrect = jsonObj['user_incorrect'];
        let accuracy = (user_correct + user_incorrect === 0) ? 100 : (100 * user_correct / (user_incorrect + user_correct)).toFixed(2);
        setUserAccuracy(accuracy);
      });
  }

  useEffect(() => {
    updateMetadata();
    if (Cookies.get('user_id') !== '') {
      updateUserData();
    }
  }, []);


  async function sendRequest() {
    const user_id = Cookies.get('user_id');
    const response = await fetch('http://localhost:8000/predictions/', {
      method: 'POST',
      body: JSON.stringify({'text': text, 'user_id': user_id}),
      headers: {
        'Content-Type': 'text/plain',
      },
    });

    const jsonObj = await response.json();
    await updateMetadata();
    if (Cookies.get('user_id') !== '') {
      updateUserData();
    }
    props.handleOpenFeedback(jsonObj['is_positive'], jsonObj['user_id']);
  }

  return (
    <div className={classes.root}>
      <Grid
        container
        spacing={1}
        direction="column"
        alignItems="center"
        justify="center"
        style={{ minHeight: '100vh' }}
      >

        <Grid item xs={12}>
          <CssBaseline />
          <Typography component="div">
            <Box fontWeight="fontWeightMedium" fontSize={24}>
              Hello, {user_id}
            </Box>
          </Typography>

          <Typography component="div">
            <Box fontWeight="fontWeightMedium" fontSize={15}>
              Total submissions: {totalSubmission} / Total positive: {totalPositive} /
              Total negative: {totalNegative} / Total Accuracy: {totalAccuracy}%
              <br/>
              Your submissions: {userSubmission} / Your positive: {userPositive} /
              Your negative: {userNegative} / Your Accuracy: {userAccuracy}%
            </Box>
          </Typography>
        </Grid>

        <Grid item xs={9}>
          <TextField
            className={classes.textField}
            id="outlined-multiline-static"
            label="Type a sentence/paragraph here and detect its sentiment:"
            multiline
            rows="10"
            required={true}
            fullWidth
            margin="normal"
            variant="outlined"
            placeholder="e.g.: I love this movie 🥰 or I think it's terrible 😤"
            onChange={(e) => setText(e.target.value)}
          />
        </Grid>
        <Grid item xs={9}>
          <Button class={classes.myButton} variant="contained" color="primary" onClick={sendRequest}>
            submit
          </Button>
        </Grid>

        <Grid item xs={9}>
          <Typography component="div">
            <Box fontWeight="fontWeightLight" fontSize={10}>
              This is a full stack/machine learning web application using AWD_LSTM model and fine-tuned on IMDB movie review dataset.
              <br/>
              Front-end: React.js, material-ui
              <br/>
              Back-end: Django, MongoDB(NoSql)
              <br/>
              Deployed on GitHub Pages and Google Cloud Platform(GCP).
              <br/>
              <Box fontWeight="fontWeightMedium" fontSize={14}>
                <Link href="https://www.linkedin.com/in/wang7ong/" target="_blank">
                  @Created by Tong Wang
                </Link>
              </Box>
            </Box>
          </Typography>
        </Grid>

      </Grid>
    </div>
  );
}