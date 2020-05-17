import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';

const useStyles = makeStyles(theme => ({
  root: {
    flexGrow: 1,
  },
  menuButton: {
    marginRight: theme.spacing(2),
  },
  title: {
    flexGrow: 1,
  },
}));

export default function NavBar(props) {
  const classes = useStyles();

  const handLogOut = () => {
    document.cookie = "user_id=";
    window.location.reload(false);
  };

  return (
      <AppBar position="static">
        <Toolbar variant='dense'>

          <IconButton edge="start" color="inherit" aria-label="menu">
            <MenuIcon />
          </IconButton>

          <Typography variant="h6" className={classes.title}>
            Sentiment Sensor
          </Typography>

          <Button
            className={classes.menuButton}
            onClick={props.onClickSignUp}
            variant="contained"
            color="default">
            Sign-Up
          </Button>

          <Button
            className={classes.menuButton}
            onClick={props.onClickSignIn}
            variant="contained"
            color="default">
            Sign-In
          </Button>

          <Button
            className={classes.menuButton}
            onClick={handLogOut}
            variant="contained"
            color="secondary">
            Sign-Out
          </Button>

        </Toolbar>
      </AppBar>
  );
}