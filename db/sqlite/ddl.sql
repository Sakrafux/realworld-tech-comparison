CREATE TABLE "app_user" (
  "id" INTEGER PRIMARY KEY AUTOINCREMENT,
  "username" TEXT UNIQUE NOT NULL,
  "email" TEXT UNIQUE NOT NULL,
  "password" TEXT NOT NULL,
  "bio" TEXT NOT NULL,
  "image" TEXT,
  "created_at" DATETIME DEFAULT CURRENT_TIMESTAMP,
  "updated_at" DATETIME DEFAULT CURRENT_TIMESTAMP,
  "version" INTEGER DEFAULT 1
);

CREATE TABLE "article" (
  "id" INTEGER PRIMARY KEY AUTOINCREMENT,
  "slug" TEXT UNIQUE NOT NULL,
  "title" TEXT UNIQUE NOT NULL,
  "description" TEXT NOT NULL,
  "body" TEXT NOT NULL,
  "fk_author" INTEGER NOT NULL,
  "created_at" DATETIME DEFAULT CURRENT_TIMESTAMP,
  "updated_at" DATETIME DEFAULT CURRENT_TIMESTAMP,
  "version" INTEGER DEFAULT 1,
  FOREIGN KEY ("fk_author") REFERENCES "app_user" ("id")
);

CREATE TABLE "comment" (
  "id" INTEGER PRIMARY KEY AUTOINCREMENT,
  "body" TEXT NOT NULL,
  "fk_article" INTEGER NOT NULL,
  "fk_author" INTEGER NOT NULL,
  "created_at" DATETIME DEFAULT CURRENT_TIMESTAMP,
  "updated_at" DATETIME DEFAULT CURRENT_TIMESTAMP,
  "version" INTEGER DEFAULT 1,
  FOREIGN KEY ("fk_article") REFERENCES "article" ("id"),
  FOREIGN KEY ("fk_author") REFERENCES "app_user" ("id")
);

CREATE TABLE "tag" (
  "id" INTEGER PRIMARY KEY AUTOINCREMENT,
  "tag" TEXT UNIQUE NOT NULL,
  "created_at" DATETIME DEFAULT CURRENT_TIMESTAMP,
  "updated_at" DATETIME DEFAULT CURRENT_TIMESTAMP,
  "version" INTEGER DEFAULT 1
);

CREATE TABLE "follow_is_user_to_user" (
  "following_user_id" INTEGER,
  "followed_user_id" INTEGER,
  PRIMARY KEY ("following_user_id", "followed_user_id"),
  FOREIGN KEY ("following_user_id") REFERENCES "app_user" ("id"),
  FOREIGN KEY ("followed_user_id") REFERENCES "app_user" ("id")
);

CREATE TABLE "tag_is_article_to_tag" (
  "article_id" INTEGER,
  "tag_id" INTEGER,
  PRIMARY KEY ("article_id", "tag_id"),
  FOREIGN KEY ("article_id") REFERENCES "article" ("id"),
  FOREIGN KEY ("tag_id") REFERENCES "tag" ("id")
);

CREATE TABLE "favorite_is_article_to_user" (
  "article_id" INTEGER,
  "user_id" INTEGER,
  PRIMARY KEY ("article_id", "user_id"),
  FOREIGN KEY ("article_id") REFERENCES "article" ("id"),
  FOREIGN KEY ("user_id") REFERENCES "app_user" ("id")
);

CREATE INDEX "ix_user_username" ON "app_user" ("username");
CREATE INDEX "ix_user_email" ON "app_user" ("email");
CREATE INDEX "ix_article_slug" ON "article" ("slug");
CREATE INDEX "ix_article_fk_author" ON "article" ("fk_author");
CREATE INDEX "ix_comment_fk_article" ON "comment" ("fk_article");
CREATE INDEX "ix_comment_fk_author" ON "comment" ("fk_author");
CREATE INDEX "ix_tag_tag" ON "tag" ("tag");
