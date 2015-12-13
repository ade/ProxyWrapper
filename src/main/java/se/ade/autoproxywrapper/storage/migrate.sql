Temporary

insert into traffic_stats (datepart_year, datepart_month, datepart_day, amount_sent, amount_received)
	select substr(s1.stat_date, 0, instr(s1.stat_date, '-')),
			substr(substr(s1.stat_date, instr(s1.stat_date, '-') + 1), 0, instr(s1.stat_date, '-') - 2),
			substr(substr(s1.stat_date, instr(s1.stat_date, '-')), instr(s1.stat_date, '-')),
			s1.amount,
			s2.amount
	from bytes_sent s1, bytes_received s2
	where s1.stat_date = s2.stat_date;