clear *
tacke(1) = struct('x', 1.5, 'y', 6, 'labela', 'x_1', 'klasa', '+', 'tezina', 0.0);
tacke(2) = struct('x', 5.5, 'y', 6, 'labela', 'x_2', 'klasa', '+', 'tezina', 0.0);
tacke(3) = struct('x', 6.9, 'y', 5.5, 'labela', 'x_3', 'klasa', 'o', 'tezina', 0.0);
tacke(4) = struct('x', 1.2, 'y', 4, 'labela', 'x_4', 'klasa', '+', 'tezina', 0.0);
tacke(5) = struct('x', 3, 'y', 4.5, 'labela', 'x_5', 'klasa', 'o', 'tezina', 0.0);
tacke(6) = struct('x', 7, 'y', 4, 'labela', 'x_6', 'klasa', 'o', 'tezina', 0.0);
tacke(7) = struct('x', 1.7, 'y', 2, 'labela', 'x_7', 'klasa', '+', 'tezina', 0.0);
tacke(8) = struct('x', 3.2, 'y', 2.5, 'labela', 'x_8', 'klasa', 'o', 'tezina', 0.0);
tacke(9) = struct('x', 6.5, 'y', 3, 'labela', 'x_9', 'klasa', 'o', 'tezina', 0.0);

for i = 1:length(tacke)
    boja = 'b';
    if tacke(i).klasa == '+'
        boja = 'r';
    end
    r = plot(tacke(i).x, tacke(i).y, tacke(i).klasa, 'MarkerSize', 10);
    set(r, 'Color', boja);
    hold on;
    tacke(i).tezina = 1.0 / length(tacke);
    text(tacke(i).x, tacke(i).y-0.25, strcat('x_', num2str(i)), 'FontName', 'Palatino Linotype');
end
xlim([0, 10]);
ylim([0, 8]);

minx = min(floor(vertcat(tacke.x)-1))
maxx = max(ceil(vertcat(tacke.x)));
for i = minx:maxx
    y(i*2+1) = struct('l', -inf, 'r', i, 'znacaj', 0.0);
    y(i*2+2) = struct('l', i, 'r', inf, 'znacaj', 0.0);
end
maksimalni_broj_iteracija = 5;
iteracija = 1;
while iteracija <= maksimalni_broj_iteracija
    greske = zeros(length(y), 1);
    for i = 1:length(y)
        for j = 1:length(tacke)
            if (tacke(j).x >= y(i).l && tacke(j).x < y(i).r && tacke(j).klasa == 'o') ...
                    || ((tacke(j).x < y(i).l || tacke(j).x >= y(i).r) && tacke(j).klasa == '+')
                greske(i) = greske(i) + tacke(j).tezina;
            end
        end
    end
    [greska, indeks] = min(greske);
    znacaj = 0.5 * log((1 - greska)/greska);
    Y(iteracija) = struct('l', y(indeks).l, 'r', y(indeks).r, 'znacaj', znacaj);
    for i = 1:length(tacke)
        if tacke(i).x >= y(indeks).l && tacke(i).x < y(indeks).r && tacke(i).klasa == '+' ...
                || ((tacke(i).x < y(indeks).l || tacke(i).x >= y(indeks).r) && tacke(i).klasa == 'o')
            tacke(i).tezina = tacke(i).tezina * 0.5 * (1 / (1 - greska));
        else
            tacke(i).tezina = tacke(i).tezina * 0.5 * (1 / greska);
        end
    end
    iteracija = iteracija + 1;
end
hold on;
for i = 1:length(Y)
    plot([Y(i).l, Y(i).l], [0, 10], 'k--');
    plot([Y(i).r, Y(i).r], [0, 10], 'k--');
end
ukupno = 0;
tacka = 3.5;
for i = 1:5
    if (Y(i).l <= tacka && Y(i).r > tacka)
        ukupno = ukupno + Y(i).znacaj;
    else
        ukupno = ukupno - Y(i).znacaj;
    end
end
sign(ukupno)