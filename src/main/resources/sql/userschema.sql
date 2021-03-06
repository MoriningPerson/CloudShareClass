--DROP TABLE IF EXISTS `Mycourse`;
CREATE TABLE IF NOT EXISTS `Mycourse` (
    `course_id` INT(11),
    `name` VARCHAR(200),
    `url` VARCHAR(200),
    `cover` VARCHAR(200),
    `origin` VARCHAR(200),
    `score` DECIMAL(10, 2),
    `counter` INT(11),
    `type` VARCHAR(200),
    `relative_id` INT(11)
);

--DROP TABLE IF EXISTS `Course`;
CREATE TABLE IF NOT EXISTS `Course`
(
    `course_id` int,
    `name` varchar(50),
    `url` varchar(200),
    cover varchar(200),
    `origin` varchar(20),
    `score` DECIMAL(10, 2),
    `type` varchar(200),
    `titleList` varchar(200),
    `universityList` varchar(200),
    `contentList` varchar(200)
);


--DROP TABLE IF EXISTS `User`;
CREATE TABLE IF NOT EXISTS `User` (
    `user_id` INT(11),
    `name` VARCHAR(200),
    `school` VARCHAR(200),
    `telephone` VARCHAR(200),
    `password` VARCHAR(200),
    `portrait_url` VARCHAR(200),
    `birth` VARCHAR(100),
    `nickname` VARCHAR(100),
    `education` VARCHAR(100),
    `sex` VARCHAR(100),
    `signature` VARCHAR(100),
    `city` VARCHAR(100)
);

--DROP TABLE IF EXISTS `Posting`;
CREATE TABLE IF NOT EXISTS `Posting` (
    `post_id` INT(11),
    `type` VARCHAR(200),
    `title` VARCHAR(200),
    `content` VARCHAR(200),
    `counter` INT(11),
    `post_time` INT(11)
);

--DROP TABLE IF EXISTS `Tag`;
CREATE TABLE IF NOT EXISTS `Tag` (
    `tag_id` INT(11),
    `tag_name` VARCHAR(200),
    `counter` INT(11),
    `tag_type` VARCHAR(200)
);


--DROP TABLE IF EXISTS `Star`;
CREATE TABLE IF NOT EXISTS `Star` (
    `course_id` INT(11),
    `user_id` INT(11)
);

--DROP TABLE IF EXISTS `Browse`;
CREATE TABLE IF NOT EXISTS `Browse` (
    `course_id` INT(11),
    `user_id` INT(11),
    `browse_time` INT(11)
);

--DROP TABLE IF EXISTS `Watch`;
CREATE TABLE IF NOT EXISTS `Watch` (
    `course_id` INT(11),
    `user_id` INT(11)
);

--DROP TABLE IF EXISTS `Relative`;
CREATE TABLE IF NOT EXISTS `Relative` (
    `course_id` INT(11),
    `post_id` INT(11)
);

--DROP TABLE IF EXISTS `Post`;
CREATE TABLE IF NOT EXISTS `Post` (
    `user_id` INT(11),
    `post_id` INT(11)
);

--DROP TABLE IF EXISTS `Comment`;
CREATE TABLE IF NOT EXISTS `Comment` (
    `user_id` INT(11),
    `post_id` INT(11),
    `content` VARCHAR(200),
    `comment_time` VARCHAR(200)
);

--DROP TABLE IF EXISTS `Has`;
CREATE TABLE IF NOT EXISTS `Has` (
    `course_id` INT(11),
    `tag_id` INT(11),
    `type` VARCHAR(200)
);

--DROP TABLE IF EXISTS `Like`;
CREATE TABLE IF NOT EXISTS `Like` (
    `user_id` INT(11),
    `tag_id` INT(11),
    `value` INT(11)
);

--DROP TABLE IF EXISTS `Rate`;
CREATE TABLE IF NOT EXISTS `Rate` (
    `user_id` INT(11),
    `course_id` INT(11),
    `rate_time` VARCHAR(200),
    `score` INT(11)
);

--DROP TABLE IF EXISTS `CourseIndex`;
CREATE TABLE IF NOT EXISTS `CourseIndex` (
    `course_id` INT(11),
    `name` VARCHAR(200),
    `url` VARCHAR(200),
    `cover` VARCHAR(200),
    `origin` VARCHAR(200),
    `type` VARCHAR(200)
);

--DROP TABLE IF EXITS `Message`;
CREATE TABLE IF NOT EXISTS `Message` (
    `user_id` INT(11),
    `course_id` INT(11)
);

--DROP TABLE IF EXITS `Recommend`;
CREATE TABLE IF NOT EXISTS `Recommend` (
    `user_id` INT(11),
    `course_id` INT(11)
);
