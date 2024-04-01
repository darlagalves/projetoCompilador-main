program completo;
var
    x, y: integer;
begin
    read(x);
    read(y);
    read(x, y);

    write(x);
    write('texto');
    write(10);

    writeln(x);
    writeln('texto');
    writeln(10);

    for x := 1 to 10 do begin
        write(x);
        write(y);
    end;
     x := 1;

    if (x >= 10) then
    begin
        write(x);
        write(y);
    end;


    x := 10;
end.
