SELECT id, data
FROM postgres.data 
WHERE data @> '{"state": "California"}';

CREATE INDEX state_index ON postgres.data 
USING GIN ((data -> 'state'));

CREATE INDEX profit_index ON postgres.data 
USING GIN ((data -> 'profit'));

SELECT id
  , data ->> 'state'
  , data ->> 'profit'
FROM postgres.data 
where cast(data ->> 'profit' AS decimal) > '85';

SELECT id, data
FROM postgres.data 
WHERE data ->> 'state' = 'California';

SELECT id
  , data ->> 'state' AS state
  , data ->> 'profit' AS profit
FROM postgres.data 
WHERE 
cast(data ->> 'profit' AS decimal) > '600';

SELECT count(*) from postgres.data;

SELECT * from postgres.data where id = 225568;

select data ->> 'state' AS state, 
max(cast(data ->> 'quantity' AS integer)) as quantity from 
postgres.data
group by data ->> 'state'
order by quantity desc;

select * from postgres.data limit 1;



select * from postgres.data limit 1;

select data ->> 'customerName' AS customerName,
max(cast(data ->> 'quantity' AS integer)) as quantity 
from 
postgres.data
where 
data @> '{"city": "Troy"}'
group by customerName;
