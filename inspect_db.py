import sqlite3
fn='viso_db_copy'
try:
    conn=sqlite3.connect(fn)
    cur=conn.cursor()
    cur.execute("SELECT id,name,createdAt FROM goals WHERE isEmergencyFund=1")
    rows=cur.fetchall()
    for r in rows:
        print('|'.join(str(x) for x in r))
    if not rows:
        print('NO_EMERGENCY_GOAL')
    conn.close()
except Exception as e:
    print('ERROR',e)
    raise
