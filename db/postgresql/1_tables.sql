CREATE TABLE "app_user" (
  "id" bigint PRIMARY KEY,
  "username" varchar(50) UNIQUE NOT NULL,
  "email" varchar(100) UNIQUE NOT NULL,
  "password" varchar(60) NOT NULL,
  "bio" varchar(255) NOT NULL,
  "image" varchar(255),
  "created_at" timestamp,
  "updated_at" timestamp,
  "version" integer
);

CREATE TABLE "article" (
  "id" bigint PRIMARY KEY,
  "slug" varchar(100) UNIQUE NOT NULL,
  "title" varchar(100) UNIQUE NOT NULL,
  "description" varchar(255) NOT NULL,
  "body" text NOT NULL,
  "fk_author" bigint NOT NULL,
  "created_at" timestamp,
  "updated_at" timestamp,
  "version" integer
);

CREATE TABLE "comment" (
  "id" bigint PRIMARY KEY,
  "body" text NOT NULL,
  "fk_article" bigint NOT NULL,
  "fk_author" bigint NOT NULL,
  "created_at" timestamp,
  "updated_at" timestamp,
  "version" integer
);

CREATE TABLE "tag" (
  "id" bigint PRIMARY KEY,
  "tag" varchar(20) UNIQUE NOT NULL,
  "created_at" timestamp,
  "updated_at" timestamp,
  "version" integer
);

CREATE TABLE "follow_is_user_to_user" (
  "following_user_id" bigint,
  "followed_user_id" bigint,
  PRIMARY KEY ("following_user_id", "followed_user_id")
);

CREATE TABLE "tag_is_article_to_tag" (
  "article_id" bigint,
  "tag_id" bigint,
  PRIMARY KEY ("article_id", "tag_id")
);

CREATE TABLE "favorite_is_article_to_user" (
  "article_id" bigint,
  "user_id" bigint,
  PRIMARY KEY ("article_id", "user_id")
);

CREATE INDEX "ix_user_username" ON "app_user" ("username");

CREATE INDEX "ix_user_email" ON "app_user" ("email");

CREATE INDEX "ix_article_slug" ON "article" ("slug");

CREATE INDEX "ix_article_fk_author" ON "article" ("fk_author");

CREATE INDEX "ix_comment_fk_article" ON "comment" ("fk_article");

CREATE INDEX "ix_comment_fk_author" ON "comment" ("fk_author");

CREATE INDEX "ix_tag_tag" ON "tag" ("tag");

ALTER TABLE "article" ADD FOREIGN KEY ("fk_author") REFERENCES "app_user" ("id");

ALTER TABLE "comment" ADD FOREIGN KEY ("fk_article") REFERENCES "article" ("id");

ALTER TABLE "comment" ADD FOREIGN KEY ("fk_author") REFERENCES "app_user" ("id");

ALTER TABLE "follow_is_user_to_user" ADD FOREIGN KEY ("following_user_id") REFERENCES "app_user" ("id");

ALTER TABLE "follow_is_user_to_user" ADD FOREIGN KEY ("followed_user_id") REFERENCES "app_user" ("id");

ALTER TABLE "tag_is_article_to_tag" ADD FOREIGN KEY ("article_id") REFERENCES "article" ("id");

ALTER TABLE "tag_is_article_to_tag" ADD FOREIGN KEY ("tag_id") REFERENCES "tag" ("id");

ALTER TABLE "favorite_is_article_to_user" ADD FOREIGN KEY ("article_id") REFERENCES "article" ("id");

ALTER TABLE "favorite_is_article_to_user" ADD FOREIGN KEY ("user_id") REFERENCES "app_user" ("id");
