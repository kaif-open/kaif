INSERT INTO ZoneInfo (zone, aliasname, theme, voteauthority, debateauthority, writeauthority,
                      adminaccountids, hidefromtop, createtime)
VALUES
  ('programming', '程式設計', 'z-theme-default', 'CITIZEN', 'CITIZEN', 'CITIZEN', '{}', FALSE, now());

INSERT INTO ZoneInfo (zone, aliasname, theme, voteauthority, debateauthority, writeauthority,
                      adminaccountids, hidefromtop, createtime)
VALUES
  ('sysop', '官方站務區', 'z-theme-default', 'CITIZEN', 'CITIZEN', 'CITIZEN', '{}', FALSE, now());

INSERT INTO ZoneInfo (zone, aliasname, theme, voteauthority, debateauthority, writeauthority,
                      adminaccountids, hidefromtop, createtime)
VALUES
  ('compiling', '哈啦閒聊的時刻', 'z-theme-default', 'CITIZEN', 'CITIZEN', 'CITIZEN', '{}', FALSE, now());

INSERT INTO ZoneInfo (zone, aliasname, theme, voteauthority, debateauthority, writeauthority,
                      adminaccountids, hidefromtop, createtime)
VALUES
  ('zone', '討論區相關事務', 'z-theme-default', 'CITIZEN', 'CITIZEN', 'CITIZEN', '{}', FALSE, now());

-- special zones:

-- test zone allow tourist to post
INSERT INTO ZoneInfo (zone, aliasname, theme, voteauthority, debateauthority, writeauthority,
                      adminaccountids, hidefromtop, createtime)
VALUES
  ('test', '測試專區', 'z-theme-default', 'TOURIST', 'TOURIST', 'TOURIST', '{}', TRUE, now());

-- official zones:
INSERT INTO ZoneInfo (zone, aliasname, theme, voteauthority, debateauthority, writeauthority,
                      adminaccountids, hidefromtop, createtime)
VALUES
  ('kaif-faq', 'Kaif 常見問題', 'z-theme-kaif', 'CITIZEN', 'CITIZEN', 'FORBIDDEN', '{}', TRUE, now());

INSERT INTO ZoneInfo (zone, aliasname, theme, voteauthority, debateauthority, writeauthority,
                      adminaccountids, hidefromtop, createtime)
VALUES
  ('kaif-terms', 'Kaif 服務條款', 'z-theme-kaif', 'CITIZEN', 'CITIZEN', 'FORBIDDEN', '{}', TRUE, now());

INSERT INTO ZoneInfo (zone, aliasname, theme, voteauthority, debateauthority, writeauthority,
                      adminaccountids, hidefromtop, createtime)
VALUES
  ('kaif-privacy', 'Kaif 隱私權政策', 'z-theme-kaif', 'CITIZEN', 'CITIZEN', 'FORBIDDEN', '{}', TRUE,
   now());
