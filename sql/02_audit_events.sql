-- ユーザー操作履歴テーブル
CREATE TABLE user_audit_events (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL,
    target_user_id BIGINT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    details JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス
CREATE INDEX idx_user_audit_events_user_id ON user_audit_events(user_id);
CREATE INDEX idx_user_audit_events_target_user_id ON user_audit_events(target_user_id);
CREATE INDEX idx_user_audit_events_created_at ON user_audit_events(created_at);
CREATE INDEX idx_user_audit_events_action ON user_audit_events(action);