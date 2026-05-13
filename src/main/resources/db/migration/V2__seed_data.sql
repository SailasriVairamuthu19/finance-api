-- password is "password123" hashed with BCrypt
INSERT INTO users (id, email, password, full_name, phone_number, role)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'john@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'John Doe',
    '9876543210',
    'USER'
);

INSERT INTO users (id, email, password, full_name, phone_number, role)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    'admin@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Admin User',
    '9000000000',
    'ADMIN'
);

INSERT INTO accounts (id, user_id, account_number, account_type, balance, currency)
VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    '11111111-1111-1111-1111-111111111111',
    'ACC0000000001',
    'SAVINGS',
    50000.00,
    'INR'
);

INSERT INTO accounts (id, user_id, account_number, account_type, balance, currency)
VALUES (
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    '11111111-1111-1111-1111-111111111111',
    'ACC0000000002',
    'CURRENT',
    150000.00,
    'INR'
);

INSERT INTO transactions (account_id, type, amount, description, reference_number, status)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'CREDIT', 50000.00, 'Initial deposit',     'REF0000000001', 'SUCCESS'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'DEBIT',  5000.00,  'ATM withdrawal',      'REF0000000002', 'SUCCESS'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'CREDIT', 10000.00, 'Salary credit',       'REF0000000003', 'SUCCESS'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'CREDIT', 150000.00,'Business deposit',    'REF0000000004', 'SUCCESS'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'DEBIT',  25000.00, 'Vendor payment',      'REF0000000005', 'SUCCESS');