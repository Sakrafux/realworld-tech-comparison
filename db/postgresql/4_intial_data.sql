insert into 
    app_user(id, username, email, password, bio)
values 
    -- the hash translates to 'password'
    (nextval('seq_user_id'), 'User', 'user@email.com', '$2y$10$f9twxaWKs5Q.1VfByd/AoOX2L.x5j2prFajUPDpt4mV2615cdx/ea', 'Just a simple user'),
    (nextval('seq_user_id'), 'MegaUser', 'megauser@email.com', '$2y$10$f9twxaWKs5Q.1VfByd/AoOX2L.x5j2prFajUPDpt4mV2615cdx/ea', 'Not just a simple user')
;

insert into 
    article(id, slug, title, description, body, fk_author)
values 
    (nextval('seq_article_id'), 'some-article', 'Some Article', 'It is great.', 'Lorem impsum...', 2)
;

insert into 
    comment(id, body, fk_article, fk_author)
values 
    (nextval('seq_comment_id'), 'I don''t like it.', 1, 1)
;

insert into 
    tag(id, tag)
values 
    (nextval('seq_tag_id'), 'filler')
;

insert into 
    follow_is_user_to_user(following_user_id, followed_user_id)
values 
    (1, 2)
;

insert into 
    tag_is_article_to_tag(article_id, tag_id)
values 
    (1, 1)
;
