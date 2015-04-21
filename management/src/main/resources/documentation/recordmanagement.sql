--
-- PostgreSQL database dump
--

-- Dumped from database version 9.2.4
-- Dumped by pg_dump version 9.2.4
-- Started on 2013-06-14 11:09:50 BST

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 179 (class 3079 OID 12328)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 2585 (class 0 OID 0)
-- Dependencies: 179
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 170 (class 1259 OID 16674)
-- Name: lastlogin; Type: TABLE; Schema: public; Owner: recordmanagement; Tablespace: 
--

CREATE TABLE lastlogin (
    id bigint NOT NULL,
    lastlogin timestamp without time zone,
    user_id bigint
);


ALTER TABLE public.lastlogin OWNER TO recordmanagement;

--
-- TOC entry 169 (class 1259 OID 16672)
-- Name: lastlogin_id_seq; Type: SEQUENCE; Schema: public; Owner: recordmanagement
--

CREATE SEQUENCE lastlogin_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.lastlogin_id_seq OWNER TO recordmanagement;

--
-- TOC entry 2586 (class 0 OID 0)
-- Dependencies: 169
-- Name: lastlogin_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: recordmanagement
--

ALTER SEQUENCE lastlogin_id_seq OWNED BY lastlogin.id;


--
-- TOC entry 172 (class 1259 OID 25851)
-- Name: logentry; Type: TABLE; Schema: public; Owner: recordmanagement; Tablespace: 
--

CREATE TABLE logentry (
    id bigint NOT NULL,
    action integer,
    message character varying(255),
    "timestamp" timestamp without time zone,
    user_id bigint
);


ALTER TABLE public.logentry OWNER TO recordmanagement;

--
-- TOC entry 171 (class 1259 OID 25849)
-- Name: logentry_id_seq; Type: SEQUENCE; Schema: public; Owner: recordmanagement
--

CREATE SEQUENCE logentry_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.logentry_id_seq OWNER TO recordmanagement;

--
-- TOC entry 2587 (class 0 OID 0)
-- Dependencies: 171
-- Name: logentry_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: recordmanagement
--

ALTER SEQUENCE logentry_id_seq OWNED BY logentry.id;


--
-- TOC entry 168 (class 1259 OID 16386)
-- Name: sequence; Type: TABLE; Schema: public; Owner: recordmanagement; Tablespace: 
--

CREATE TABLE sequence (
    seq_name character varying(50) NOT NULL,
    seq_count numeric(38,0)
);


ALTER TABLE public.sequence OWNER TO recordmanagement;

--
-- TOC entry 174 (class 1259 OID 25859)
-- Name: session; Type: TABLE; Schema: public; Owner: recordmanagement; Tablespace: 
--

CREATE TABLE session (
    id bigint NOT NULL,
    lastlogin timestamp without time zone,
    user_id bigint
);


ALTER TABLE public.session OWNER TO recordmanagement;

--
-- TOC entry 173 (class 1259 OID 25857)
-- Name: session_id_seq; Type: SEQUENCE; Schema: public; Owner: recordmanagement
--

CREATE SEQUENCE session_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.session_id_seq OWNER TO recordmanagement;

--
-- TOC entry 2588 (class 0 OID 0)
-- Dependencies: 173
-- Name: session_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: recordmanagement
--

ALTER SEQUENCE session_id_seq OWNED BY session.id;


--
-- TOC entry 176 (class 1259 OID 25867)
-- Name: systemobj; Type: TABLE; Schema: public; Owner: recordmanagement; Tablespace: 
--

CREATE TABLE systemobj (
    id bigint NOT NULL,
    type integer,
    url character varying(255)
);


ALTER TABLE public.systemobj OWNER TO recordmanagement;

--
-- TOC entry 175 (class 1259 OID 25865)
-- Name: systemobj_id_seq; Type: SEQUENCE; Schema: public; Owner: recordmanagement
--

CREATE SEQUENCE systemobj_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.systemobj_id_seq OWNER TO recordmanagement;

--
-- TOC entry 2589 (class 0 OID 0)
-- Dependencies: 175
-- Name: systemobj_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: recordmanagement
--

ALTER SEQUENCE systemobj_id_seq OWNED BY systemobj.id;


--
-- TOC entry 178 (class 1259 OID 25875)
-- Name: userobj; Type: TABLE; Schema: public; Owner: recordmanagement; Tablespace: 
--

CREATE TABLE userobj (
    id bigint NOT NULL,
    name character varying(255),
    password character varying(255),
    role integer,
    surname character varying(255),
    username character varying(255)
);


ALTER TABLE public.userobj OWNER TO recordmanagement;

--
-- TOC entry 177 (class 1259 OID 25873)
-- Name: userobj_id_seq; Type: SEQUENCE; Schema: public; Owner: recordmanagement
--

CREATE SEQUENCE userobj_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.userobj_id_seq OWNER TO recordmanagement;

--
-- TOC entry 2590 (class 0 OID 0)
-- Dependencies: 177
-- Name: userobj_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: recordmanagement
--

ALTER SEQUENCE userobj_id_seq OWNED BY userobj.id;


--
-- TOC entry 2544 (class 2604 OID 16677)
-- Name: id; Type: DEFAULT; Schema: public; Owner: recordmanagement
--

ALTER TABLE ONLY lastlogin ALTER COLUMN id SET DEFAULT nextval('lastlogin_id_seq'::regclass);


--
-- TOC entry 2545 (class 2604 OID 25854)
-- Name: id; Type: DEFAULT; Schema: public; Owner: recordmanagement
--

ALTER TABLE ONLY logentry ALTER COLUMN id SET DEFAULT nextval('logentry_id_seq'::regclass);


--
-- TOC entry 2546 (class 2604 OID 25862)
-- Name: id; Type: DEFAULT; Schema: public; Owner: recordmanagement
--

ALTER TABLE ONLY session ALTER COLUMN id SET DEFAULT nextval('session_id_seq'::regclass);


--
-- TOC entry 2547 (class 2604 OID 25870)
-- Name: id; Type: DEFAULT; Schema: public; Owner: recordmanagement
--

ALTER TABLE ONLY systemobj ALTER COLUMN id SET DEFAULT nextval('systemobj_id_seq'::regclass);


--
-- TOC entry 2548 (class 2604 OID 25878)
-- Name: id; Type: DEFAULT; Schema: public; Owner: recordmanagement
--

ALTER TABLE ONLY userobj ALTER COLUMN id SET DEFAULT nextval('userobj_id_seq'::regclass);


--
-- TOC entry 2569 (class 0 OID 16674)
-- Dependencies: 170
-- Data for Name: lastlogin; Type: TABLE DATA; Schema: public; Owner: recordmanagement
--

COPY lastlogin (id, lastlogin, user_id) FROM stdin;
\.


--
-- TOC entry 2591 (class 0 OID 0)
-- Dependencies: 169
-- Name: lastlogin_id_seq; Type: SEQUENCE SET; Schema: public; Owner: recordmanagement
--

SELECT pg_catalog.setval('lastlogin_id_seq', 1, false);


--
-- TOC entry 2571 (class 0 OID 25851)
-- Dependencies: 172
-- Data for Name: logentry; Type: TABLE DATA; Schema: public; Owner: recordmanagement
--


--
-- TOC entry 2592 (class 0 OID 0)
-- Dependencies: 171
-- Name: logentry_id_seq; Type: SEQUENCE SET; Schema: public; Owner: recordmanagement
--

SELECT pg_catalog.setval('logentry_id_seq', 187, true);


--
-- TOC entry 2567 (class 0 OID 16386)
-- Dependencies: 168
-- Data for Name: sequence; Type: TABLE DATA; Schema: public; Owner: recordmanagement
--

COPY sequence (seq_name, seq_count) FROM stdin;
SEQ_GEN	0
\.


--
-- TOC entry 2573 (class 0 OID 25859)
-- Dependencies: 174
-- Data for Name: session; Type: TABLE DATA; Schema: public; Owner: recordmanagement
--

COPY session (id, lastlogin, user_id) FROM stdin;
1	2013-06-12 17:52:49.409	1
\.


--
-- TOC entry 2593 (class 0 OID 0)
-- Dependencies: 173
-- Name: session_id_seq; Type: SEQUENCE SET; Schema: public; Owner: recordmanagement
--

SELECT pg_catalog.setval('session_id_seq', 1, true);


--
-- TOC entry 2575 (class 0 OID 25867)
-- Dependencies: 176
-- Data for Name: systemobj; Type: TABLE DATA; Schema: public; Owner: recordmanagement
--

COPY systemobj (id, type, url) FROM stdin;
2	0	http://localhost:9999/solr/search
3	1	localhost
\.


--
-- TOC entry 2594 (class 0 OID 0)
-- Dependencies: 175
-- Name: systemobj_id_seq; Type: SEQUENCE SET; Schema: public; Owner: recordmanagement
--

SELECT pg_catalog.setval('systemobj_id_seq', 3, true);


--
-- TOC entry 2577 (class 0 OID 25875)
-- Dependencies: 178
-- Data for Name: userobj; Type: TABLE DATA; Schema: public; Owner: recordmanagement
--

COPY userobj (id, name, password, role, surname, username) FROM stdin;
1	ymamakis	0158212af0ba0cb770e71dacf4ef2d80	0	mamakis	ymamakis
\.


--
-- TOC entry 2595 (class 0 OID 0)
-- Dependencies: 177
-- Name: userobj_id_seq; Type: SEQUENCE SET; Schema: public; Owner: recordmanagement
--

SELECT pg_catalog.setval('userobj_id_seq', 1, true);


--
-- TOC entry 2552 (class 2606 OID 16679)
-- Name: lastlogin_pkey; Type: CONSTRAINT; Schema: public; Owner: recordmanagement; Tablespace: 
--

ALTER TABLE ONLY lastlogin
    ADD CONSTRAINT lastlogin_pkey PRIMARY KEY (id);


--
-- TOC entry 2554 (class 2606 OID 25856)
-- Name: logentry_pkey; Type: CONSTRAINT; Schema: public; Owner: recordmanagement; Tablespace: 
--

ALTER TABLE ONLY logentry
    ADD CONSTRAINT logentry_pkey PRIMARY KEY (id);


--
-- TOC entry 2550 (class 2606 OID 16390)
-- Name: sequence_pkey; Type: CONSTRAINT; Schema: public; Owner: recordmanagement; Tablespace: 
--

ALTER TABLE ONLY sequence
    ADD CONSTRAINT sequence_pkey PRIMARY KEY (seq_name);


--
-- TOC entry 2556 (class 2606 OID 25864)
-- Name: session_pkey; Type: CONSTRAINT; Schema: public; Owner: recordmanagement; Tablespace: 
--

ALTER TABLE ONLY session
    ADD CONSTRAINT session_pkey PRIMARY KEY (id);


--
-- TOC entry 2558 (class 2606 OID 25872)
-- Name: systemobj_pkey; Type: CONSTRAINT; Schema: public; Owner: recordmanagement; Tablespace: 
--

ALTER TABLE ONLY systemobj
    ADD CONSTRAINT systemobj_pkey PRIMARY KEY (id);


--
-- TOC entry 2562 (class 2606 OID 25897)
-- Name: uk_6cb1641e42664db0aef6b28e3a4; Type: CONSTRAINT; Schema: public; Owner: recordmanagement; Tablespace: 
--

ALTER TABLE ONLY userobj
    ADD CONSTRAINT uk_6cb1641e42664db0aef6b28e3a4 UNIQUE (username);


--
-- TOC entry 2560 (class 2606 OID 25895)
-- Name: uk_da830165ede64bd4afc7a426ddf; Type: CONSTRAINT; Schema: public; Owner: recordmanagement; Tablespace: 
--

ALTER TABLE ONLY systemobj
    ADD CONSTRAINT uk_da830165ede64bd4afc7a426ddf UNIQUE (url);


--
-- TOC entry 2564 (class 2606 OID 25883)
-- Name: userobj_pkey; Type: CONSTRAINT; Schema: public; Owner: recordmanagement; Tablespace: 
--

ALTER TABLE ONLY userobj
    ADD CONSTRAINT userobj_pkey PRIMARY KEY (id);


--
-- TOC entry 2565 (class 2606 OID 25884)
-- Name: fk_13d02e85df0049b1b16f1e578c4; Type: FK CONSTRAINT; Schema: public; Owner: recordmanagement
--

ALTER TABLE ONLY logentry
    ADD CONSTRAINT fk_13d02e85df0049b1b16f1e578c4 FOREIGN KEY (user_id) REFERENCES userobj(id);


--
-- TOC entry 2566 (class 2606 OID 25889)
-- Name: fk_fe392ae74c1746eb842e0694e6b; Type: FK CONSTRAINT; Schema: public; Owner: recordmanagement
--

ALTER TABLE ONLY session
    ADD CONSTRAINT fk_fe392ae74c1746eb842e0694e6b FOREIGN KEY (user_id) REFERENCES userobj(id);


--
-- TOC entry 2584 (class 0 OID 0)
-- Dependencies: 5
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2013-06-14 11:09:50 BST

--
-- PostgreSQL database dump complete
--

