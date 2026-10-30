-- Reference data for StaySafe. It is loaded into the database the first time the
-- app runs; after that the app only reads these values from the database tables.
-- Hotels, rooms, accounts and bookings are NOT here: users create them in the app.
-- To add a city or payment method later, add a line here and reinstall the app.

INSERT INTO cities (name) VALUES ('Lilongwe');
INSERT INTO cities (name) VALUES ('Blantyre');
INSERT INTO cities (name) VALUES ('Mzuzu');
INSERT INTO cities (name) VALUES ('Zomba');

INSERT INTO room_types (name) VALUES ('Standard Room');
INSERT INTO room_types (name) VALUES ('Deluxe Room');
INSERT INTO room_types (name) VALUES ('Suite');

INSERT INTO payment_methods (name, short_label, color) VALUES ('Airtel Money', 'airtel', '#E4002B');
INSERT INTO payment_methods (name, short_label, color) VALUES ('TNM Mpamba', 'TNM', '#2E9E44');

INSERT INTO settings (key, value) VALUES ('upfront_percent', '20');
