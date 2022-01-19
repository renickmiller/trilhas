import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { IPontosTuristicos, PontosTuristicos } from '../pontos-turisticos.model';
import { PontosTuristicosService } from '../service/pontos-turisticos.service';
import { IPontosCardeais } from 'app/entities/pontos-cardeais/pontos-cardeais.model';
import { PontosCardeaisService } from 'app/entities/pontos-cardeais/service/pontos-cardeais.service';
import { ITiposPontoTuristico } from 'app/entities/tipos-ponto-turistico/tipos-ponto-turistico.model';
import { TiposPontoTuristicoService } from 'app/entities/tipos-ponto-turistico/service/tipos-ponto-turistico.service';
import { ITrilhas } from 'app/entities/trilhas/trilhas.model';
import { TrilhasService } from 'app/entities/trilhas/service/trilhas.service';

@Component({
  selector: 'jhi-pontos-turisticos-update',
  templateUrl: './pontos-turisticos-update.component.html',
})
export class PontosTuristicosUpdateComponent implements OnInit {
  isSaving = false;

  pontosCardeaisCollection: IPontosCardeais[] = [];
  tiposPontoTuristicosCollection: ITiposPontoTuristico[] = [];
  trilhasSharedCollection: ITrilhas[] = [];

  editForm = this.fb.group({
    id: [],
    nome: [],
    descricao: [],
    pontosCardeais: [],
    tiposPontoTuristico: [],
    trilhas: [],
  });

  constructor(
    protected pontosTuristicosService: PontosTuristicosService,
    protected pontosCardeaisService: PontosCardeaisService,
    protected tiposPontoTuristicoService: TiposPontoTuristicoService,
    protected trilhasService: TrilhasService,
    protected activatedRoute: ActivatedRoute,
    protected fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ pontosTuristicos }) => {
      this.updateForm(pontosTuristicos);

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const pontosTuristicos = this.createFromForm();
    if (pontosTuristicos.id !== undefined) {
      this.subscribeToSaveResponse(this.pontosTuristicosService.update(pontosTuristicos));
    } else {
      this.subscribeToSaveResponse(this.pontosTuristicosService.create(pontosTuristicos));
    }
  }

  trackPontosCardeaisById(index: number, item: IPontosCardeais): number {
    return item.id!;
  }

  trackTiposPontoTuristicoById(index: number, item: ITiposPontoTuristico): number {
    return item.id!;
  }

  trackTrilhasById(index: number, item: ITrilhas): number {
    return item.id!;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPontosTuristicos>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(pontosTuristicos: IPontosTuristicos): void {
    this.editForm.patchValue({
      id: pontosTuristicos.id,
      nome: pontosTuristicos.nome,
      descricao: pontosTuristicos.descricao,
      pontosCardeais: pontosTuristicos.pontosCardeais,
      tiposPontoTuristico: pontosTuristicos.tiposPontoTuristico,
      trilhas: pontosTuristicos.trilhas,
    });

    this.pontosCardeaisCollection = this.pontosCardeaisService.addPontosCardeaisToCollectionIfMissing(
      this.pontosCardeaisCollection,
      pontosTuristicos.pontosCardeais
    );
    this.tiposPontoTuristicosCollection = this.tiposPontoTuristicoService.addTiposPontoTuristicoToCollectionIfMissing(
      this.tiposPontoTuristicosCollection,
      pontosTuristicos.tiposPontoTuristico
    );
    this.trilhasSharedCollection = this.trilhasService.addTrilhasToCollectionIfMissing(
      this.trilhasSharedCollection,
      pontosTuristicos.trilhas
    );
  }

  protected loadRelationshipsOptions(): void {
    this.pontosCardeaisService
      .query({ filter: 'pontosturisticos-is-null' })
      .pipe(map((res: HttpResponse<IPontosCardeais[]>) => res.body ?? []))
      .pipe(
        map((pontosCardeais: IPontosCardeais[]) =>
          this.pontosCardeaisService.addPontosCardeaisToCollectionIfMissing(pontosCardeais, this.editForm.get('pontosCardeais')!.value)
        )
      )
      .subscribe((pontosCardeais: IPontosCardeais[]) => (this.pontosCardeaisCollection = pontosCardeais));

    this.tiposPontoTuristicoService
      .query({ filter: 'pontosturisticos-is-null' })
      .pipe(map((res: HttpResponse<ITiposPontoTuristico[]>) => res.body ?? []))
      .pipe(
        map((tiposPontoTuristicos: ITiposPontoTuristico[]) =>
          this.tiposPontoTuristicoService.addTiposPontoTuristicoToCollectionIfMissing(
            tiposPontoTuristicos,
            this.editForm.get('tiposPontoTuristico')!.value
          )
        )
      )
      .subscribe((tiposPontoTuristicos: ITiposPontoTuristico[]) => (this.tiposPontoTuristicosCollection = tiposPontoTuristicos));

    this.trilhasService
      .query()
      .pipe(map((res: HttpResponse<ITrilhas[]>) => res.body ?? []))
      .pipe(map((trilhas: ITrilhas[]) => this.trilhasService.addTrilhasToCollectionIfMissing(trilhas, this.editForm.get('trilhas')!.value)))
      .subscribe((trilhas: ITrilhas[]) => (this.trilhasSharedCollection = trilhas));
  }

  protected createFromForm(): IPontosTuristicos {
    return {
      ...new PontosTuristicos(),
      id: this.editForm.get(['id'])!.value,
      nome: this.editForm.get(['nome'])!.value,
      descricao: this.editForm.get(['descricao'])!.value,
      pontosCardeais: this.editForm.get(['pontosCardeais'])!.value,
      tiposPontoTuristico: this.editForm.get(['tiposPontoTuristico'])!.value,
      trilhas: this.editForm.get(['trilhas'])!.value,
    };
  }
}