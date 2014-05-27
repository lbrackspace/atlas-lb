all: pcrevalidate

pcrevalidate.o: pcrevalidate.c 
	cc -O0 -ggdb -c pcrevalidate.c

pcrevalidate: pcrevalidate.o
	cc -O0 -ggdb -o pcrevalidate pcrevalidate.o -lpcre

clean:
	rm *.o pcrevalidate
