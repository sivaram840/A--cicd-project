-- V3__create_expenses_and_shares.sql
CREATE TABLE IF NOT EXISTS expenses (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  group_id BIGINT NOT NULL,
  created_by BIGINT NOT NULL,
  payer_id BIGINT NOT NULL,
  amount DECIMAL(19,2) NOT NULL,
  currency VARCHAR(10) NOT NULL DEFAULT 'INR',
  split_type VARCHAR(20) NOT NULL, -- EQUAL, PERCENT, CUSTOM
  note VARCHAR(1000),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_expenses_group FOREIGN KEY (group_id) REFERENCES `groups`(id) ON DELETE CASCADE,
  CONSTRAINT fk_expenses_created_by FOREIGN KEY (created_by) REFERENCES users(id),
  CONSTRAINT fk_expenses_payer FOREIGN KEY (payer_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS expense_shares (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  expense_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  share_amount DECIMAL(19,2) NOT NULL,
  is_settled BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_shares_expense FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
  CONSTRAINT fk_shares_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
