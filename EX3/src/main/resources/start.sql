CREATE USER geo WITH PASSWORD 'geo';

SELECT 'CREATE DATABASE osm OWNER geo ENCODING ''UTF8'''
WHERE NOT EXISTS(SELECT FROM pg_database WHERE datname = 'osm')
\gexec;