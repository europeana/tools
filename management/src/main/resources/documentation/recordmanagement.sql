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

COPY logentry (id, action, message, "timestamp", user_id) FROM stdin;
1	0		2013-06-07 17:51:02.173	1
2	4	retrieved all systems	2013-06-07 17:51:02.337	1
3	4	retrieved all users	2013-06-07 17:51:02.388	1
4	0		2013-06-07 17:56:14.662	1
5	4	retrieved all systems	2013-06-07 17:56:14.864	1
6	4	retrieved all users	2013-06-07 17:56:14.904	1
7	0		2013-06-07 18:00:51.899	1
8	4	retrieved all systems	2013-06-07 18:00:52.064	1
9	4	retrieved all users	2013-06-07 18:00:52.18	1
10	0		2013-06-07 18:04:27.028	1
11	4	retrieved all systems	2013-06-07 18:04:27.18	1
12	4	retrieved all users	2013-06-07 18:04:27.199	1
13	0		2013-06-07 18:13:47.912	1
14	4	retrieved all systems	2013-06-07 18:13:48.059	1
15	4	retrieved all users	2013-06-07 18:13:48.079	1
16	0		2013-06-07 18:17:43.194	1
17	4	retrieved all systems	2013-06-07 18:17:43.344	1
18	4	retrieved all users	2013-06-07 18:17:43.369	1
19	0		2013-06-07 18:20:35.915	1
20	4	retrieved all systems	2013-06-07 18:20:36.093	1
21	4	retrieved all users	2013-06-07 18:20:36.14	1
22	0		2013-06-07 18:27:20.727	1
23	4	retrieved all systems	2013-06-07 18:27:20.974	1
24	4	retrieved all users	2013-06-07 18:27:21.035	1
25	0		2013-06-07 18:35:00.836	1
26	4	retrieved all systems	2013-06-07 18:35:01.026	1
27	4	retrieved all users	2013-06-07 18:35:01.077	1
28	0		2013-06-07 18:39:05.892	1
29	4	retrieved all systems	2013-06-07 18:39:06.135	1
30	4	retrieved all users	2013-06-07 18:39:06.227	1
31	0		2013-06-07 18:41:46.54	1
32	4	retrieved all systems	2013-06-07 18:41:46.742	1
33	4	retrieved all users	2013-06-07 18:41:46.783	1
34	0		2013-06-07 18:47:16.394	1
35	4	retrieved all systems	2013-06-07 18:47:16.551	1
36	4	retrieved all users	2013-06-07 18:47:16.587	1
37	0		2013-06-07 18:54:10.575	1
38	4	retrieved all systems	2013-06-07 18:54:10.743	1
39	4	retrieved all users	2013-06-07 18:54:10.784	1
40	0		2013-06-10 10:36:48.923	1
41	4	retrieved all systems	2013-06-10 10:36:49.201	1
42	4	retrieved all users	2013-06-10 10:36:49.216	1
43	0		2013-06-10 10:38:19.456	1
44	4	retrieved all systems	2013-06-10 10:38:19.647	1
45	4	retrieved all users	2013-06-10 10:38:19.659	1
46	0		2013-06-10 10:44:19.512	1
47	4	retrieved all systems	2013-06-10 10:44:19.726	1
48	4	retrieved all users	2013-06-10 10:44:19.761	1
49	3	added system	2013-06-10 10:44:57.107	1
50	4	retrieved all systems	2013-06-10 10:44:58.297	1
51	0		2013-06-10 10:58:40.696	1
52	4	retrieved all users	2013-06-10 10:58:41.009	1
53	4	retrieved all systems	2013-06-10 10:58:41.009	1
54	0		2013-06-10 11:07:05.892	1
55	4	retrieved all systems	2013-06-10 11:07:06.473	1
56	4	retrieved all users	2013-06-10 11:07:06.667	1
57	0		2013-06-10 11:23:32.84	1
58	4	retrieved all users	2013-06-10 11:23:33.472	1
59	4	retrieved all systems	2013-06-10 11:23:33.477	1
60	0		2013-06-10 11:29:51.808	1
61	4	retrieved all systems	2013-06-10 11:29:52.278	1
62	4	retrieved all users	2013-06-10 11:29:52.281	1
63	1	removed collection 09102	2013-06-10 11:30:03.192	1
64	4	found systems	2013-06-10 11:31:57.192	1
65	0		2013-06-10 11:39:31.426	1
66	4	retrieved all users	2013-06-10 11:39:33.679	1
67	4	retrieved all systems	2013-06-10 11:39:33.756	1
68	4	found systems	2013-06-10 11:47:19.783	1
69	4	found systems	2013-06-10 11:47:20.043	1
70	4	found systems	2013-06-10 11:47:20.075	1
71	4	found systems	2013-06-10 11:47:20.122	1
72	4	found systems	2013-06-10 11:47:20.151	1
73	4	found systems	2013-06-10 11:47:20.176	1
74	4	found systems	2013-06-10 11:47:20.207	1
75	4	found systems	2013-06-10 11:47:20.25	1
76	4	found systems	2013-06-10 11:47:20.276	1
77	4	found systems	2013-06-10 11:47:20.321	1
78	4	found systems	2013-06-10 11:47:20.355	1
79	4	found systems	2013-06-10 11:47:20.4	1
80	4	found systems	2013-06-10 11:47:20.427	1
81	4	found systems	2013-06-10 11:47:20.45	1
82	3	added system	2013-06-10 11:47:45.841	1
83	4	retrieved all systems	2013-06-10 11:47:47.048	1
84	1	deleted system	2013-06-10 11:47:48.837	1
85	4	retrieved all systems	2013-06-10 11:47:50.117	1
86	4	found systems	2013-06-10 11:48:04.482	1
87	4	found systems	2013-06-10 11:48:04.501	1
88	4	found systems	2013-06-10 11:48:04.549	1
89	4	found systems	2013-06-10 11:48:04.591	1
90	4	found systems	2013-06-10 11:48:04.623	1
91	4	found systems	2013-06-10 11:48:04.66	1
92	4	found systems	2013-06-10 11:48:04.684	1
93	4	found systems	2013-06-10 11:48:04.71	1
94	4	found systems	2013-06-10 11:48:04.74	1
95	4	found systems	2013-06-10 11:48:04.77	1
96	4	found systems	2013-06-10 11:48:04.805	1
97	4	found systems	2013-06-10 11:48:04.846	1
98	4	found systems	2013-06-10 11:48:04.868	1
99	4	found systems	2013-06-10 11:48:04.898	1
100	0		2013-06-10 12:14:41.395	1
101	0		2013-06-10 12:14:41.341	1
102	4	retrieved all users	2013-06-10 12:14:42.423	1
103	4	retrieved all users	2013-06-10 12:14:42.502	1
104	4	retrieved all systems	2013-06-10 12:14:42.507	1
105	4	retrieved all systems	2013-06-10 12:14:42.508	1
106	4	found systems	2013-06-10 12:15:53.059	1
107	4	found systems	2013-06-10 12:15:53.789	1
108	4	found systems	2013-06-10 12:15:55.995	1
109	4	found systems	2013-06-10 12:15:56.068	1
110	4	found systems	2013-06-10 12:15:56.153	1
111	4	found systems	2013-06-10 12:15:56.218	1
112	4	found systems	2013-06-10 12:15:56.472	1
113	4	found systems	2013-06-10 12:15:56.503	1
114	4	found systems	2013-06-10 12:15:56.595	1
115	4	found systems	2013-06-10 12:15:56.637	1
116	4	found systems	2013-06-10 12:15:56.822	1
117	4	found systems	2013-06-10 12:15:56.912	1
118	4	found systems	2013-06-10 12:15:56.961	1
119	4	found systems	2013-06-10 12:15:56.995	1
120	0		2013-06-10 13:48:28.573	1
121	4	retrieved all users	2013-06-10 13:48:28.884	1
122	4	retrieved all systems	2013-06-10 13:48:28.894	1
123	0		2013-06-10 14:00:41.137	1
124	4	retrieved all systems	2013-06-10 14:00:41.429	1
125	4	retrieved all users	2013-06-10 14:00:41.453	1
126	4	found all logs	2013-06-10 14:14:59.979	1
127	4	found user logs	2013-06-10 14:16:32.479	1
128	3	added system	2013-06-10 14:20:30.992	1
129	4	retrieved all systems	2013-06-10 14:20:32.297	1
130	1	removed collection 2022026	2013-06-10 14:21:23.067	1
131	0		2013-06-10 18:21:42.258	1
132	4	retrieved all systems	2013-06-10 18:21:42.665	1
133	4	retrieved all users	2013-06-10 18:21:42.668	1
134	4	found user logs	2013-06-10 18:22:45.092	1
135	3	created user username	2013-06-12 09:54:16.661	\N
136	3	created user username	2013-06-12 09:55:32.715	\N
178	0		2013-06-12 17:37:01.59	1
179	4	retrieved all users	2013-06-12 17:37:02.203	1
180	0		2013-06-12 17:52:49.409	1
181	4	retrieved all users	2013-06-12 17:52:49.719	1
182	4	retrieved all systems	2013-06-12 17:52:49.741	1
183	5	updated user ymamakis	2013-06-12 18:02:03.399	1
184	4	found user logs	2013-06-12 18:06:21.104	1
185	4	found all logs	2013-06-12 18:09:09.101	1
186	4	found user logs	2013-06-12 18:09:13.5	1
187	4	found all logs	2013-06-12 18:09:15.132	1
\.


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

