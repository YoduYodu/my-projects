package models;

import hotel.InvalidRatingException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Review model
 */
public class Review implements Comparable<Review> {
    private String hotelId;
    private String reviewId;
    private int averageRating;
    private String reviewTitle;
    private String reviewText;
    private boolean isRecom;
    private Date reviewSubmissionTime;
    private String userNickname;

    /**
     * Constructor
     * @param hotelId
     * @param reviewId
     * @param averageRating
     * @param reviewTitle
     * @param reviewText
     * @param userNickname
     * @param reviewSubmissionTime
     * @param isRecom
     */
    public Review(String hotelId, String reviewId, int averageRating,
                  String reviewTitle, String reviewText, String userNickname, String reviewSubmissionTime, boolean isRecom) throws InvalidRatingException, ParseException{
        this.hotelId = hotelId;
        this.reviewId = reviewId;
        validateRating(averageRating);
        this.averageRating = averageRating;
        this.reviewTitle = reviewTitle;
        this.reviewText = reviewText;
        this.userNickname = userNickname;
        this.reviewSubmissionTime = parseDate(reviewSubmissionTime);
        this.isRecom = isRecom;
    }

    /**
     * Validate rating
     * @param rating
     */
    private void validateRating(int rating) throws InvalidRatingException {
        if (rating > 5 || rating < 0) {
            throw new InvalidRatingException("Rating out of range (0-5)");
        }
    }

    /**
     * Parse date
     * @param reviewSubmissionTime reviewSubmissionTime
     */
    private Date parseDate(String reviewSubmissionTime) throws ParseException{
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            return format.parse(reviewSubmissionTime);
        } catch (ParseException e) {
            return format2.parse(reviewSubmissionTime);
        }
    }

    /**
     * Readable string
     * @return Readable string
     */
    @Override
    public String toString() {
        return "{" +
                "hotelId=" + hotelId +
                "reviewId='" + reviewId + '\'' +
                "averageRating=" + averageRating +
                "title='" + reviewTitle + '\'' +
                "reviewText='" + reviewText + '\'' +
                "userNickname='" + userNickname + '\'' +
                "reviewSubmissionTime='" + reviewSubmissionTime +
                '}';
    }

    /**
     * Getter for hotel id.
     * @return hotel id
     */
    public String getHotelId() {
        return hotelId;
    }

    /**
     * Getter for review id
     * @return review id
     */
    public String getReviewId() { return reviewId; }

    /**
     * Getter for average rating.
     * @return
     */
    public int getAverageRating() {
        return averageRating;
    }

    /**
     * Getter for title
     * @return title
     */
    public String getReviewTitle() {
        return reviewTitle;
    }

    /**
     * Getter for review text
     * @return review text
     */
    public String getReviewText() {
        return reviewText;
    }

    /**
     * Getter for user nickname
     * @return review text
     */
    public String getUserNickname() { return userNickname; }

    /**
     * Getter for review submission time
     * @return eview submission time
     */
    public Date getReviewSubmissionTime() {
        return reviewSubmissionTime;
    }

    /* Get isRecommend */
    public boolean getIsRecommend() {
        return isRecom;
    }

    /**
     * Comparable review
     * @param other other review
     * @return To make the reviews sorted by from most recent to oldest, and by user nickname
     */
    @Override
    public int compareTo(Review other) {
        int compareTime = other.reviewSubmissionTime.compareTo(this.reviewSubmissionTime);
        if (compareTime != 0) {
            return compareTime;
        }
        int compareNickName = this.userNickname.compareTo(other.userNickname);
        if (compareNickName != 0) {
            return compareNickName;
        }
        return this.reviewId.compareTo(other.reviewId);
    }

    /**
     * Equals
     * @param o another object
     * @return true if they are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Review)) {
            return false;
        } else {
            return this.reviewId.equals(((Review) o).reviewId);
        }
    }
}
